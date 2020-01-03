# CircleCI

## Description

This setup will automate Github releases by creating a new release with a ready-for-users zipfile of your mod every time a git tag is pushed to your GitHub repository.

## Steps

1. Copy the `.circleci` folder to your mod directory.
1. Open `.circleci/zipmod.sh` with a text editor (e.g. VS Code, Notepad++). Change the value for `modFolderName`. This will be the name of the mod folder.
     - It's best practice not to use spaces, e.g. "Gates-Awakened".
1. Create an account at [CircleCI](https://circleci.com/signup/) and select "Sign Up With GitHub".
1. Select "Add Projects" and choose your repository to set it up.
1. Create a [new Personal Access Token](https://github.com/settings/tokens/new) in Github - select ` write:packages` and ` delete:packages` (others will automatically get selected). Copy the token.
     - This will allow CircleCI to create new Github releases automatically.
1. On CircleCI, open the Settings page for your new project, select Environment Variables, and click Add Variable. Name it `"GITHUB_TOKEN"` and paste the token that you had copied.
1. That's it! The next time you push a git tag (`git tag 1.0.0 && git push 1.0.0`), CircleCI will automatically create a zip file containing your mod name with the version appended, then create a new GitHub release with the zip file.
   - You will need to update the release's changelog manually.
