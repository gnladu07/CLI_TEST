
import json
import os
import subprocess
import sys
from pathlib import Path

# macOS에서 npm / Homebrew로 설치한 Codex 실행 경로
os.environ["PATH"] = os.pathsep.join([
    str(Path.home() / ".npm-global/bin"),
    "/opt/homebrew/bin",
    "/usr/local/bin",
    os.environ.get("PATH", ""),
])


def respond(value=None):
    """Cursor Hook에 JSON 결과 전달"""
    print(json.dumps(value or {}, ensure_ascii=False))


def run_git(*args):
    """Git 명령어 실행"""
    return subprocess.run(
        ["git", *args],
        check=True,
        capture_output=True,
        text=True
    ).stdout.strip()


def main():
    event = sys.argv[1]
    data = json.load(sys.stdin)

    # Git 내부 폴더에 상태와 리뷰 결과 저장
    git_dir = Path(run_git("rev-parse", "--absolute-git-dir"))
    state_dir = git_dir / "cursor-codex"
    state_dir.mkdir(parents=True, exist_ok=True)

    pending = state_dir / "pending"
    report = state_dir / "last-review.md"

    # 1. Cursor가 파일을 수정한 경우
    if event == "edited":
        pending.touch()
        respond()
        return

    if event != "stop":
        respond()
        return

    # 2. 정상 완료 상태만 처리
    if data.get("status") != "completed":
        pending.unlink(missing_ok=True)
        respond()
        return

    # 3. 변경이 없으면 리뷰 생략
    if not pending.exists() or not run_git("status", "--porcelain"):
        pending.unlink(missing_ok=True)
        respond()
        return

    # 4. Codex 자동 코드 리뷰
    # 읽기 전용 모드: Codex는 코드 수정 불가
    result = subprocess.run(
        [
            "codex",
            "-c", 'sandbox_mode="read-only"',
            "review",
            "--uncommitted"
        ],
        capture_output=True,
        text=True,
        stdin=subprocess.DEVNULL,
        timeout=540
    )

    # 5. 리뷰 실패 처리
    if result.returncode != 0 or not result.stdout.strip():
        report.write_text(
            "Codex review failed.\n"
            + (result.stderr or result.stdout),
            encoding="utf-8"
        )
        print(
            "Codex review failed; see " + str(report),
            file=sys.stderr
        )
        respond()
        return

    # 6. 리뷰 결과 저장
    review = result.stdout.strip()
    report.write_text(review + "\n", encoding="utf-8")
    pending.unlink(missing_ok=True)

    # 7. 반복 제한: 최대 2회 자동 후속 수정 기회
    if int(data.get("loop_count", 0)) >= 2:
        respond()
        return

    # 8. 리뷰 결과를 Cursor Agent에 자동 전달
    respond({
        "followup_message": (
            "Codex가 현재 Git 변경사항을 리뷰했습니다. "
            "다음 의견을 기존 구현과 테스트로 검증하세요. "
            "실제 결함만 수정하고 관련 테스트를 실행하세요. "
            "문제가 없으면 파일을 수정하지 말고 "
            "최종 완료 보고를 하세요. "
            "자동 커밋·푸시는 금지합니다.\n\n"
            + review[:12000]
        )
    })


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"Review hook error: {error}", file=sys.stderr)
        respond()
