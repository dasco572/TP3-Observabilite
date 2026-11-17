#!/usr/bin/env sh
# Enveloppe de confort pour macOS, Linux et Git Bash.
# Aucune logique ici : tout est dans le script Node, qui fait foi.
exec node "$(dirname "$0")/verifier-confidentialite.mjs" "$@"
