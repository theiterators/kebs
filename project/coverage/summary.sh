#!/bin/sh
# a Markdown table of the statement and branch coverage of each module, from the reports of `sbt testCoverage`
echo "| Module | Statements | Branches |"
echo "|---|---|---|"
find . -name scoverage.xml -path "*scoverage-report*" | sort | while read -r report; do
  module=$(echo "$report" | sed -E 's#^\./([^/]+)/.*#\1#')
  statements=$(grep -m1 -oE 'statement-rate="[^"]*"' "$report" | sed -E 's/.*"(.*)"/\1/')
  branches=$(grep -m1 -oE 'branch-rate="[^"]*"' "$report" | sed -E 's/.*"(.*)"/\1/')
  echo "| $module | $statements% | $branches% |"
done
