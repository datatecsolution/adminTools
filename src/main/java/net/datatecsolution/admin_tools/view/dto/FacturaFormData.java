package net.datatecsolution.admin_tools.view.dto;

import net.datatecsolution.admin_tools.modelo.DetalleFactura;
import net.datatecsolution.admin_tools.modelo.Factura;

import java.util.Collections;
import java.util.List;

/**
 * Snapshot agregado de los campos editables del formulario de
 * facturación. Compone los cluster-DTOs existentes
 * ({@link FacturaCabeceraData}, {@link FacturaClienteData}) más la
 * lista de detalles, para permitir al controller manipular el estado
 * de la forma con una sola llamada
 * ({@code view.setFormData(...)}/{@code view.getFormData()}) en vez
 * de coordinar 4-5 getters/setters cluster por separado.
 *
 * No incluye totales (son derivados) ni botones (son UI affordance,
 * no estado de formulario). Esos siguen manejándose por separado.
 */
public class FacturaFormData {

    private FacturaCabeceraData cabecera;
    private FacturaClienteData cliente;
    private List<DetalleFactura> detalles;

    public FacturaFormData() {
        this.cabecera = new FacturaCabeceraData();
        this.cliente = new FacturaClienteData();
        this.detalles = Collections.emptyList();
    }

    public FacturaFormData(FacturaCabeceraData cabecera, FacturaClienteData cliente,
                           List<DetalleFactura> detalles) {
        this.cabecera = cabecera;
        this.cliente = cliente;
        this.detalles = detalles;
    }

    // ─── Factories ──────────────────────────────────────────────────────────

    /** Formulario en blanco: contado, consumidor final, sin detalles. */
    public static FacturaFormData empty(String fechaSistema) {
        return new FacturaFormData(
                new FacturaCabeceraData(FacturaCabeceraData.TIPO_CONTADO, fechaSistema),
                new FacturaClienteData(1, "Consumidor final", ""),
                Collections.emptyList()
        );
    }

    /** Formulario poblado desde una factura existente (carga de orden pendiente, edición). */
    public static FacturaFormData of(Factura factura, String fecha) {
        FacturaClienteData clienteData = factura.getCliente() != null
                ? new FacturaClienteData(
                        factura.getCliente().getId(),
                        factura.getCliente().getNombre(),
                        factura.getCliente().getRtn())
                : new FacturaClienteData(1, "Consumidor final", "");

        return new FacturaFormData(
                new FacturaCabeceraData(factura.getTipoFactura(), fecha),
                clienteData,
                factura.getDetalles()
        );
    }

    // ─── Getters / setters ──────────────────────────────────────────────────

    public FacturaCabeceraData getCabecera() {
        return cabecera;
    }

    public void setCabecera(FacturaCabeceraData cabecera) {
        this.cabecera = cabecera;
    }

    public FacturaClienteData getCliente() {
        return cliente;
    }

    public void setCliente(FacturaClienteData cliente) {
        this.cliente = cliente;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
    }
}
