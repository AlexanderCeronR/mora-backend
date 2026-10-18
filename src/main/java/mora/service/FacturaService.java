package mora.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.draw.LineSeparator;
import mora.entity.DetallePedido;
import mora.entity.Pago;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;



@Service
public class FacturaService {

    // Datos del restaurante
    private static final String NOMBRE_RESTAURANTE = "MORA Restaurant";
    private static final String NIT = "900.123.456-7";
    private static final String DIRECCION = "Calle 123 # 45-67, Medellín";
    private static final String TELEFONO = "+57 300 123 4567";
    private static final String EMAIL = "contacto@mora.com";

    // Colores MORA
    private static final BaseColor COLOR_PRIMARY =
            new BaseColor(68, 49, 153);    // #443199
    private static final BaseColor COLOR_ACCENT =
            new BaseColor(193, 51, 131);   // #C13383
    private static final BaseColor COLOR_DARK =
            new BaseColor(17, 17, 24);     // #111118
    private static final BaseColor COLOR_GRAY =
            new BaseColor(100, 116, 139);  // #64748b
    private static final BaseColor COLOR_LIGHT =
            new BaseColor(240, 240, 255);  // #f0f0ff

    public byte[] generarFacturaPDF(Pago pago) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // Fuentes
        Font fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 24,
                Font.BOLD, COLOR_PRIMARY);
        Font fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 10,
                Font.NORMAL, COLOR_GRAY);
        Font fuenteNormal = new Font(Font.FontFamily.HELVETICA, 10,
                Font.NORMAL, BaseColor.BLACK);
        Font fuenteNegrita = new Font(Font.FontFamily.HELVETICA, 10,
                Font.BOLD, BaseColor.BLACK);
        Font fuenteBlanco = new Font(Font.FontFamily.HELVETICA, 10,
                Font.BOLD, BaseColor.WHITE);
        Font fuenteTotal = new Font(Font.FontFamily.HELVETICA, 14,
                Font.BOLD, COLOR_PRIMARY);

        // ── HEADER ──
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.5f, 1f});
        header.setSpacingAfter(20);

        // Logo y nombre
        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setPadding(10);

        Paragraph nombreRest = new Paragraph(NOMBRE_RESTAURANTE,
                fuenteTitulo);
        celdaLogo.addElement(nombreRest);

        Paragraph tagline = new Paragraph(
                "Manager Of Restaurant Administration",
                fuenteSubtitulo);
        celdaLogo.addElement(tagline);

        Paragraph datosRest = new Paragraph();
        datosRest.setSpacingBefore(8);
        datosRest.add(new Chunk("NIT: " + NIT + "\n", fuenteSubtitulo));
        datosRest.add(new Chunk(DIRECCION + "\n", fuenteSubtitulo));
        datosRest.add(new Chunk(TELEFONO + "\n", fuenteSubtitulo));
        datosRest.add(new Chunk(EMAIL, fuenteSubtitulo));
        celdaLogo.addElement(datosRest);
        header.addCell(celdaLogo);

        // Info de la factura
        PdfPCell celdaFactura = new PdfPCell();
        celdaFactura.setBorder(Rectangle.NO_BORDER);
        celdaFactura.setPadding(10);
        celdaFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph tituloFactura = new Paragraph("FACTURA DE VENTA",
                new Font(Font.FontFamily.HELVETICA, 16,
                        Font.BOLD, COLOR_ACCENT));
        tituloFactura.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(tituloFactura);

        Paragraph numFactura = new Paragraph(
                pago.getNumeroFactura(),
                new Font(Font.FontFamily.HELVETICA, 12,
                        Font.BOLD, COLOR_PRIMARY));
        numFactura.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(numFactura);

        Paragraph fechaFactura = new Paragraph(
                "Fecha: " + pago.getFechaPago().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                fuenteSubtitulo);
        fechaFactura.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(fechaFactura);

        Paragraph mesaInfo = new Paragraph(
                "Mesa: " + pago.getPedido().getMesa().getNumero(),
                fuenteSubtitulo);
        mesaInfo.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(mesaInfo);

        header.addCell(celdaFactura);
        document.add(header);

        // Línea separadora
        LineSeparator line = new LineSeparator();
        line.setLineColor(COLOR_PRIMARY);
        line.setLineWidth(2);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);

        // ── TABLA DE PRODUCTOS ──
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4f, 1f, 1.5f, 1.5f});
        tabla.setSpacingBefore(10);
        tabla.setSpacingAfter(10);

        // Headers de la tabla
        String[] headers = {"PRODUCTO", "CANT.", "PRECIO", "SUBTOTAL"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fuenteBlanco));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(8);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }

        // Filas de productos
        boolean colorAlternado = false;
        for (DetallePedido detalle : pago.getPedido().getDetalles()) {
            BaseColor bgColor = colorAlternado
                    ? new BaseColor(245, 245, 255)
                    : BaseColor.WHITE;

            PdfPCell celdaNombre = new PdfPCell(
                    new Phrase(detalle.getProducto().getNombre(),
                            fuenteNormal));
            celdaNombre.setBackgroundColor(bgColor);
            celdaNombre.setPadding(7);
            celdaNombre.setBorder(Rectangle.BOTTOM);
            celdaNombre.setBorderColor(new BaseColor(230, 230, 240));
            tabla.addCell(celdaNombre);

            PdfPCell celdaCantidad = new PdfPCell(
                    new Phrase(String.valueOf(detalle.getCantidad()),
                            fuenteNormal));
            celdaCantidad.setBackgroundColor(bgColor);
            celdaCantidad.setPadding(7);
            celdaCantidad.setBorder(Rectangle.BOTTOM);
            celdaCantidad.setBorderColor(new BaseColor(230, 230, 240));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaCantidad);

            PdfPCell celdaPrecio = new PdfPCell(
                    new Phrase("$" + detalle.getProducto().getPrecio()
                            .toPlainString(), fuenteNormal));
            celdaPrecio.setBackgroundColor(bgColor);
            celdaPrecio.setPadding(7);
            celdaPrecio.setBorder(Rectangle.BOTTOM);
            celdaPrecio.setBorderColor(new BaseColor(230, 230, 240));
            celdaPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(celdaPrecio);

            PdfPCell celdaSubtotal = new PdfPCell(
                    new Phrase("$" + detalle.getSubtotal()
                            .toPlainString(), fuenteNegrita));
            celdaSubtotal.setBackgroundColor(bgColor);
            celdaSubtotal.setPadding(7);
            celdaSubtotal.setBorder(Rectangle.BOTTOM);
            celdaSubtotal.setBorderColor(new BaseColor(230, 230, 240));
            celdaSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(celdaSubtotal);

            colorAlternado = !colorAlternado;
        }

        document.add(tabla);

        // ── TOTALES ──
        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(40);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setWidths(new float[]{1.5f, 1f});
        totales.setSpacingBefore(5);

        // Subtotal sin IVA
        java.math.BigDecimal subtotalSinIva = pago.getTotal()
                .divide(new java.math.BigDecimal("1.19"),
                        2, java.math.RoundingMode.HALF_UP);

        java.math.BigDecimal iva = pago.getTotal()
                .subtract(subtotalSinIva);

        agregarFilaTotales(totales, "Subtotal:",
                "$" + subtotalSinIva.toPlainString(),
                fuenteNormal, false);
        agregarFilaTotales(totales, "IVA (19%):",
                "$" + iva.toPlainString(),
                fuenteNormal, false);

        // Fila total
        PdfPCell celdaTotalLabel = new PdfPCell(
                new Phrase("TOTAL:", fuenteTotal));
        celdaTotalLabel.setBorder(Rectangle.TOP);
        celdaTotalLabel.setBorderColor(COLOR_PRIMARY);
        celdaTotalLabel.setPadding(8);
        celdaTotalLabel.setBackgroundColor(
                new BaseColor(240, 240, 255));
        totales.addCell(celdaTotalLabel);

        PdfPCell celdaTotalValor = new PdfPCell(
                new Phrase("$" + pago.getTotal().toPlainString(),
                        fuenteTotal));
        celdaTotalValor.setBorder(Rectangle.TOP);
        celdaTotalValor.setBorderColor(COLOR_PRIMARY);
        celdaTotalValor.setPadding(8);
        celdaTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotalValor.setBackgroundColor(
                new BaseColor(240, 240, 255));
        totales.addCell(celdaTotalValor);

        document.add(totales);

        // ── MÉTODO DE PAGO ──
        document.add(Chunk.NEWLINE);
        PdfPTable metodoPagoTabla = new PdfPTable(1);
        metodoPagoTabla.setWidthPercentage(100);

        PdfPCell celdaMetodo = new PdfPCell();
        celdaMetodo.setBorder(Rectangle.BOX);
        celdaMetodo.setBorderColor(COLOR_PRIMARY);
        celdaMetodo.setPadding(10);
        celdaMetodo.setBackgroundColor(new BaseColor(245, 245, 255));

        Paragraph metodo = new Paragraph();
        metodo.add(new Chunk("Método de pago: ", fuenteNegrita));
        metodo.add(new Chunk(pago.getMetodoPago().name(), fuenteNormal));
        celdaMetodo.addElement(metodo);

        if (pago.getPedido() != null &&
                pago.getPedido().getMesero() != null) {

            Paragraph mesero = new Paragraph();
            mesero.add(new Chunk("Atendido por: ", fuenteNegrita));
            mesero.add(new Chunk(
                    pago.getPedido().getMesero().getNombre(),
                    fuenteNormal));
            celdaMetodo.addElement(mesero);
        }

        metodoPagoTabla.addCell(celdaMetodo);
        document.add(metodoPagoTabla);

        // ── FOOTER ──
        document.add(Chunk.NEWLINE);
        LineSeparator lineFooter = new LineSeparator();
        lineFooter.setLineColor(COLOR_ACCENT);
        lineFooter.setLineWidth(1);
        document.add(new Chunk(lineFooter));

        Paragraph footer = new Paragraph(
                "\n¡Gracias por visitarnos! " +
                        "Este documento es generado por MORA - " +
                        "Manager Of Restaurant Administration",
                new Font(Font.FontFamily.HELVETICA, 8,
                        Font.ITALIC, COLOR_GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        if (pago.getCufe() != null) {
            Paragraph cufe = new Paragraph(
                    "CUFE: " + pago.getCufe(),
                    new Font(Font.FontFamily.HELVETICA, 7,
                            Font.NORMAL, COLOR_GRAY));
            cufe.setAlignment(Element.ALIGN_CENTER);
            document.add(cufe);
        }

        document.close();
        return baos.toByteArray();
    }

    private void agregarFilaTotales(PdfPTable tabla, String label,
                                    String valor, Font fuente, boolean esTotal) {
        PdfPCell celdaLabel = new PdfPCell(new Phrase(label, fuente));
        celdaLabel.setBorder(Rectangle.NO_BORDER);
        celdaLabel.setPadding(5);
        tabla.addCell(celdaLabel);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPadding(5);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celdaValor);
    }

    // Método para obtener el campo mesero del pago
    // ya que no tiene relación directa
    private String getMeseroNombre(Pago pago) {
        if (pago.getPedido() != null &&
                pago.getPedido().getMesero() != null) {
            return pago.getPedido().getMesero().getNombre();
        }
        return "N/A";
    }
}