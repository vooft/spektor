### Required variables
- ORG_GRADLE_PROJECT_mavenCentralUsername - username from generated token at https://central.sonatype.com/account 
- ORG_GRADLE_PROJECT_mavenCentralPassword - password from generated token at https://central.sonatype.com/account
- ORG_GRADLE_PROJECT_signingInMemoryKey - `gpg --export-secret-keys --armor <key id> | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'`
- ORG_GRADLE_PROJECT_signingInMemoryKeyId - last 8 characters of the long key id that was displayed after generation
- ORG_GRADLE_PROJECT_signingInMemoryKeyPassword - password used during gpg key generation

### GPG key generation
https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair
