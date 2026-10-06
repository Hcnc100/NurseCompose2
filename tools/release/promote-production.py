"""Promote the identical successfully tested Internal binary, without rebuilding."""
import base64
import json
import os
import subprocess
from pathlib import Path

from google.oauth2 import service_account
from google.auth.transport.requests import AuthorizedSession


def main():
    def tree(ref):
        return subprocess.check_output(["git", "rev-parse", f"{ref}^{{tree}}"], text=True).strip()

    if os.environ["GITHUB_REF"] != "refs/heads/master":
        raise RuntimeError("Production is restricted to master")
    current_tree = tree("HEAD")
    runs = json.loads((Path(os.environ["RUNNER_TEMP"]) / "internal-runs.json").read_text())["workflow_runs"]
    version_code = None
    for run in runs:
        if run["conclusion"] != "success":
            continue
        try:
            candidate_tree = tree(run["head_sha"])
        except subprocess.CalledProcessError:
            continue
        if candidate_tree == current_tree:
            version_code = str(1000 + run["run_number"])
            break
    if version_code is None:
        raise RuntimeError("No successful Internal run matches master's exact tree. Test this source on Internal first.")
    secret = os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON", "")
    if not secret:
        raise RuntimeError("Missing GOOGLE_PLAY_SERVICE_ACCOUNT_JSON")
    credentials = service_account.Credentials.from_service_account_info(
        json.loads(base64.b64decode(secret, validate=True)),
        scopes=["https://www.googleapis.com/auth/androidpublisher"],
    )
    session = AuthorizedSession(credentials)
    base = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.nullpointer.nourseCompose/edits"

    def request(method, url, **kwargs):
        response = session.request(method, url, timeout=90, **kwargs)
        if not response.ok:
            raise RuntimeError(f"Play API {method} failed: HTTP {response.status_code}")
        return response.json() if response.content else {}

    edit_url = f"{base}/{request('POST', base, json={})['id']}"
    committed = False
    try:
        internal = request("GET", f"{edit_url}/tracks/internal")
        release = next((r for r in internal.get("releases", []) if r.get("status") == "completed" and version_code in r.get("versionCodes", [])), None)
        if release is None:
            raise RuntimeError("Matching tested version is not active in Internal")
        existing = request("GET", f"{edit_url}/tracks/production").get("releases", [])
        if any(version_code in r.get("versionCodes", []) for r in existing):
            print(f"Version {version_code} already exists in Production; no change.")
            return
        if any(int(c) >= int(version_code) for r in existing for c in r.get("versionCodes", [])):
            raise RuntimeError("Refusing an older Production version")
        promoted = {"status": "completed", "versionCodes": [version_code]}
        for key in ("name", "releaseNotes"):
            if key in release:
                promoted[key] = release[key]
        request("PUT", f"{edit_url}/tracks/production", json={"track": "production", "releases": [promoted]})
        request("POST", f"{edit_url}:validate")
        request("POST", f"{edit_url}:commit", params={
            "changesNotSentForReview": "false",
            "changesInReviewBehavior": "ERROR_IF_IN_REVIEW",
        })
        committed = True
        print(f"Version {version_code} submitted to Production; Google review/managed publishing may apply.")
    finally:
        if not committed:
            request("DELETE", edit_url)


if __name__ == "__main__":
    main()
