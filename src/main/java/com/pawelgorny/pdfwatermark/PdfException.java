package com.pawelgorny.pdfwatermark;

/**
 * Exception thrown during the processing of PDF if any problem occurs
 */
public class PdfException extends Exception {

    private static final long serialVersionUID = -1582837924692083135L;

    public PdfException() {
    }

    public PdfException(String message) {
        super(message);
    }

    public PdfException(Throwable cause) {
        super(cause);
    }


}
