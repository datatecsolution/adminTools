#!/bin/bash
# Los 16 dominios del proxy + nginx -t. Uso: verificar.sh
for d in admintools.supermercadosurbina.com lafe.datatecsolution.com mariposasdoradas.datatecsolution.com posdulce.datatecsolution.com; do
  printf "  %-38s POS %s  API %s\n" $d "$(curl -s -o /dev/null -m 10 -w %{http_code} https://$d/)" "$(curl -s -o /dev/null -m 10 -w %{http_code} https://$d/admin_tools/api/sellers)"
done
for d in pedidos.supermercadosurbina.com pedidos.distribuidorasharon.com pedidosmariposas.datatecsolution.com pos.datatecsolution.com catalogo.datatecsolution.com catalogodulce.datatecsolution.com datatecsolution.com corosjb.datatecsolution.com tania-david.datatecsolution.com eprof.datatecsolution.com rag.distribuidorasharon.com api-whatsapp.datatecsolution.com; do
  printf "  %-38s %s\n" $d "$(curl -s -o /dev/null -m 10 -w %{http_code} https://$d/)"
done
docker exec nginx-proxy-manager nginx -t 2>&1 | tail -1 | sed "s/^/  /"
