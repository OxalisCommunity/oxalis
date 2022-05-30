Git Checkout
Install TortoiseGit + GitHub Desktop, and clone https://github.com/mySupply/oxalis.git

Install Maven: https://mkyong.com/maven/how-to-install-maven-in-windows/

Update (directly in the browser, by pulling changed from difi)
* https://github.com/mySupply/oxalis
* https://github.com/mySupply/oxalis-as4

Update the local clone.
Update the submodule: oxalis-extension\Oxalis-AS4

You can use CMD to update the submodule (oxalis-as4) like this (if you have downloaded local clone via HTTPS through for example GitHub Desktop):
1. On Github.com, fetch upstream and merge changes into oxalis-as4 repository's master branch
2. Call "git update submodule --remote" from CMD on the forked Oxalis repository
3. "git add ."
4. "git commit -m "submodule updated"
5. "git push origin"

* Note - the build order is not always correct - but just build a couple of times.
Call 'mvn package --fail-at-end'