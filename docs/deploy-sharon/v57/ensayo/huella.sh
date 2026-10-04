#!/bin/bash
# Conteo exacto de filas por tabla base de las BDs admin_tools* (puerto por argumento)
B=/opt/homebrew/opt/mysql@8.0/bin
P=${1:-3307}
Q=$($B/mysql --no-defaults -uroot -h127.0.0.1 -P$P -N -e "SET SESSION group_concat_max_len=10000000; SELECT GROUP_CONCAT(CONCAT('SELECT ''',table_schema,'.',table_name,''', COUNT(*) FROM \`',table_schema,'\`.\`',table_name,'\`') SEPARATOR ' UNION ALL ') FROM information_schema.tables WHERE table_schema LIKE 'admin_tools%' AND table_type='BASE TABLE'")
$B/mysql --no-defaults -uroot -h127.0.0.1 -P$P -N -e "$Q" | sort
