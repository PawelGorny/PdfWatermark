package com.pawelgorny.pdfwatermark;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface PDFWatermarkService {
    /**
     * Generates a PDF File based in the input PDF file with a watermark.
     *
     * @param input    Input stream with the original PDF file
     * @param output   Output stream. It will contain the watermarked PDF file
     * @param lines    Text of the watermark to be applied
     * @param settings Settings for the watermark
     * @throws PdfException
     */
    void stamp(InputStream input, OutputStream output, List<String> lines, PdfWatermarkSettings settings) throws PdfException;
}
