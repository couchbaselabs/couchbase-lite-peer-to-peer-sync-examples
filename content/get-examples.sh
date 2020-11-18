#!/bin/bash

if [$# -eq 0]
  then
    paramOption="all"
  else
    case $1 in
      ios|xamarin|all)
        paramOption=$1
      ;;
      *)
        echo "invalid start start option $1"
        exit 1
      ;;
    esac
fi

case "$paramOption" in

  ios|all)
    echo "Processing iOS code samples"
    cd ../ios/list-sync
    touch code-samples.swift
    echo "Complete Swift code samples from which these are extracted can be found in the /ios directory at the top-level of this repo." > code-samples.swift
    echo "Extracted Date: $(date)" >> code-samples.cs # ib11182020
    ### Outer loop, selects files
    #FILES="list-sync/discovery/ServiceAdvertiser.swift" "list-sync/discovery/ServiceBrowser.swift" "list-sync/model/DatabaseManager.swift" "list-sync/model/ListRecord.swift" "list-sync/presenter/ListPresenter.swift"
    IFS=$'\n'; set -f
    FILES="$(find list-sync -type f -name '*.swift')"
    for f in $FILES
    do
        echo $f

        printf "\n//\n// Tags from %s\n//\n" $f >> code-samples.swift

        # Get *all* tagnames
        # Ensures we can loop over complete nested snippets
        tagname=`awk 'BEGIN { FS = "::"} /tag/ {print $2}' $f`

        # Inner loop
        for t in $tagname
        do
            awk 'BEGIN { FS = "::"} ($2 == p), ($2 == p && $1 ~ /end/) { print $0 }' p="$t" $f >> code-samples.swift
        done
    done
    unset IFS; set +f

    mv ./code-samples.swift ../../content/modules/cbl-p2p-sync-websockets/examples/

    cd ../../content/
  ;;

  xamarin|all)
    echo "Processing Xamarin/C# (dotnet) code samples"
    ##
    ## Loop through /dotnet code examples to get tagged code samples
    ## added  ib11182020
    cd ../dotnet/P2PListSync
    touch code-samples.cs
    echo "The complete C#/.Net code samples from which these samples are extracted can be found in the /dotnet directory at the top-level of this repo." > code-samples.cs
    echo "Extracted Date: $(date)" >> code-samples.cs
    IFS=$'\n'; set -f
    FILES="$(find P2PListSync -type f -name '*.cs')"
    for f in $FILES
    do
        echo $f

        printf "\n//\n// Tags from %s\n//\n" $f >> code-samples.cs

        # Get *all* tagnames
        # Ensures we can loop over complete nested snippets
        tagname=`awk 'BEGIN { FS = "::"} /tag/ {print $2}' $f`

        # Inner loop
        for t in $tagname
        do
            awk 'BEGIN { FS = "::"} ($2 == p), ($2 == p && $1 ~ /end/) { print $0 }' p="$t" $f >> code-samples.cs
        done
    done
    unset IFS; set +f

    mv ./code-samples.cs ../../content/modules/cbl-p2p-sync-websockets/examples/

    cd ../../content/
  ;;
  ##
  ## Similar loops here when other language repos are ready
  ##

esac

echo "Processing completed -- OK"
exit 0
