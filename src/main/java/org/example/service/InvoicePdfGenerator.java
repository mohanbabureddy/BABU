package org.example.service;

import org.example.model.TenantBill;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class InvoicePdfGenerator {

    private final TemplateEngine templateEngine;

    public InvoicePdfGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateInvoicePdf(TenantBill bill) throws Exception {
        Context ctx = new Context();
        ctx.setVariable("bill", bill);
        ctx.setVariable("PaidDate",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        String html = templateEngine.process("invoice", ctx);

        // Set base URI to resolve relative image paths
        String baseUri = new java.io.File("src/main/resources/static").toURI().toString();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }
}