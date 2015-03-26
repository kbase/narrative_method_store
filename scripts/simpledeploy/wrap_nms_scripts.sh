#!/bin/sh

#
# Wrap the NMS scripts within the NMS repo so it can be run outside of
# the KBase runtime for the docs team who shouldn't be installing the
# full KBase runtime.
#

if [ $# -ne 3 ] ; then
    echo "Usage: $0 source dest lib" 1>&2 
    exit 1
fi

src=$1
dst=$2
lib=$3

cat > $dst <<EOF1
#!/bin/sh
export PERL5LIB=$lib:\$PATH
perl $src "\$@"
EOF1

chmod +x $dst
