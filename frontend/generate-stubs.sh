set -e
src="${1}"
out="src/app/generated"

# clean the repo
if [[ -d "${out}" ]]
then
  rm -rf "${out}"
fi

# generate stubs
openapi-generator-cli generate \
  -g typescript-angular \
	-i "${src}" \
	-c 'swagger-generator.conf.json' \
	-o src/app/generated \
	--additional-properties ngVersion=10.0.0

# cleanup
rm -rf "${out}/.openapi-generator" "${out}/.openapi-generator-ignore" \
  "${out}/git_push.sh" "${out}/ng-package.json" "${out}/package.json" \
  "${out}/tsconfig.json"
