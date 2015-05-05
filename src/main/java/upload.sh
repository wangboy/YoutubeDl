dllog()
{
    echo "=========================================="
    echo $1
    echo "=========================================="
}

retry=20

upload()
{
    dir=$2
    fileName=$1
    #upload
    sleep 3
    suc=0
    for (( i = 0; i < $retry; i++ )); do
        ../../bpcs_uploader.php upload $fileName /youtube/$dir/$fileName > php.log
        result=$(tail -n 2 php.log | grep uploaded)

        if [[ -n $result ]]; then
            dllog " upload [ $fileName ] success at times $i "
            suc=1
            break
        fi
        sleep 3
    done

    rm -fr php.log

    if [[ suc -ne 1 ]]; then
        dllog "=== upload [$fileName] length fail !!!!!!!!!!!!!!!!!!!!!!!!"
        echo "$1 $fileName" >> $dir"_"fail.log
        return 0
    fi
    rm -fr $fileName
}


if [[ ! $1 ]]; then
    echo " ===== param is empty ====="
    exit 0
fi

while [[ 1 ]]; do
    sleep 5
    filelist=`ls -Sr . | grep .mp4 `
    for file in $filelist
    do
        echo "==== begin upload [ $file ]"
        # dlFile $file
        # mv $file ..
        upload $file $1
    done
done
