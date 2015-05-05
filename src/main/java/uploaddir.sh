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
        ../../../bpcs_uploader.php upload $fileName /youtube/$dir/$fileName > php.log
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

uploadDir()
{
    dir=$1
    cd $dir
    filelist=`ls -Sr . | grep .mp4 `
    for file in $filelist
    do
        echo "==== begin upload [ $file ] to dir [ $dir ]"
        upload $file $1
    done
    cd ..
}

check()
{
    while [[ 1 ]]; do
        sleep 5
        filelist=`ls . | grep -v .sh | grep -v nohup.out `
        for file in $filelist
        do
            if  [[  -d  $file ]];then
                echo "==== begin upload from dir [ $file ]"
                uploadDir $file
            fi
        done
    done
}

check
