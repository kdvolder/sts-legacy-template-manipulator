workdir=$(pwd)
unpackdir=${workdir}/unpack
repackdir=${workdir}/repack

rm -fr ${repackdir}
mkdir -p ${repackdir}

for i in ${unpackdir}/* ; do 
    echo $i
    cp -aR $i $repackdir
done

cd $repackdir
for i in * ; do
    cd $repackdir/$i
    if [ -d template ]; then
        zip -r template.zip template
        rm -fr template
    fi
    cd $repackdir/$i
    zip -r ../$i.zip .
    cd $repackdir
    rm -fr $i
done
