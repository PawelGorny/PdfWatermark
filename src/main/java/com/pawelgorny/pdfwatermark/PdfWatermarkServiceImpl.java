package com.pawelgorny.pdfwatermark;


import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.*;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class PdfWatermarkServiceImpl implements PDFWatermarkService {

    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PdfWatermarkServiceImpl.class);

    private static final String LOG_ERROR_WATERMARK_POSITION_NOT_FOUND = "Watermark position not found in available values";
    private static final double HALF_PI = Math.PI / 2;
    private static final int UNDERLINED_FONT_ADDSPACE_DIVSOR = 4;
    private static final int ANGLE_90 = 90;
    private static final int ANGLE_180 = 180;
    private static final int ANGLE_270 = 270;

    /*@Value("#{'${pdfEncryptionOwner}'}")*/ // for encrypted files
    private String pdfEncryptionOwner = "";

    public void stamp(InputStream input, OutputStream output, List<String> lines, PdfWatermarkSettings settings) throws PdfException {
        try {
            byte[]ownerPassword=null;
            if (pdfEncryptionOwner!=null && !pdfEncryptionOwner.isEmpty()){
                ownerPassword=pdfEncryptionOwner.getBytes();
            }
            PdfReader reader = new PdfReader(input, ownerPassword);
            PdfStamper pdfStamper = new PdfStamper(reader, output, '\0', true);

            int numPages = reader.getNumberOfPages();

            // Temporary variables used to check if the configuration page has been changed. If so, we need to calculate the new variables for the page
            int lastWidth = -1;
            int lastHeight = -1;
            InternalWatermarkVariables previousVariables = null;

            for (int pageNumber = 1; pageNumber <= numPages; pageNumber++) {
                InternalWatermarkVariables variables = new InternalWatermarkVariables(pageNumber);

                setDefaultWidthAndHeightFromPage(reader, variables);
                if (previousVariables != null && variables.getPageHeight() == lastHeight && variables.getPageWidth() == lastWidth) {
                    variables = previousVariables;
                    variables.setPageNumber(pageNumber);
                } else {
                    setPageDiagonalLength(variables, settings.getWatermarkPosition());
                    setPhrases(variables, settings, lines);
                    setCalculatedVariables(variables, settings);
                }


                writeText(pdfStamper, settings, variables);
                previousVariables = variables;
                lastWidth = variables.getPageWidth();
                lastHeight = variables.getPageHeight();

                if (pageNumber==1 && settings.isFirstPageOnly()){
                    break;
                }
            }

            if (settings.getInfos()!=null) {
                Map<String, String> info = reader.getInfo();
                for (Map.Entry<String , String> entry : settings.getInfos().entrySet()){
                    info.put(entry.getKey(), entry.getValue());
                }
                pdfStamper.setMoreInfo(info);
            }

            pdfStamper.close();
            reader.close();
        } catch (DocumentException | IOException e1) {
            LOGGER.error("Error (from Itext) when working with watermarking", e1);
            throw new PdfException(e1);
        }
    }

    private void writeText(PdfStamper pdfStamper, PdfWatermarkSettings settings, InternalWatermarkVariables variables) {
        PdfContentByte canvas = getCanvas(pdfStamper, settings, variables.getPageNumber());

        if (variables.getAngle() == 0) {
            for (int i = 0; i < variables.getPhrases().size(); i++) {
                Phrase phrase = variables.getPhrases().get(i);
                ColumnText.showTextAligned(canvas, settings.getAlign().getElementAlign(), phrase,
                        variables.getPageWidth(),
                        variables.getPageHeight() - i * phrase.getFont().getSize(),
                        variables.getAngle());
            }
        } else if (variables.getAngle() == ANGLE_270) {
            for (int i = 0; i < variables.getPhrases().size(); i++) {
                Phrase phrase = variables.getPhrases().get(i);
                ColumnText.showTextAligned(canvas, settings.getAlign().getElementAlign(), phrase,
                        variables.getPageWidth() - i * phrase.getFont().getSize(),
                        variables.getPageHeight(),
                        variables.getAngle());
            }
        } else if (variables.getAngle() == ANGLE_90) {
            for (int i = 0; i < variables.getPhrases().size(); i++) {
                Phrase phrase = variables.getPhrases().get(i);
                ColumnText.showTextAligned(canvas, settings.getAlign().getElementAlign(), phrase,
                        variables.getPageWidth() + i * phrase.getFont().getSize(),
                        variables.getPageHeight(),
                        variables.getAngle());
            }
        } else {
            if (variables.getPhrases().size() == 1) {
                Phrase phrase = variables.getPhrases().get(0);
                ColumnText.showTextAligned(canvas, settings.getAlign().getElementAlign(), phrase,
                        variables.getPageWidth(),
                        variables.getPageHeight(),
                        variables.getAngle());
            } else {
                for (int i = 0; i < variables.getPhrases().size(); i++) {
                    Phrase phrase = variables.getPhrases().get(i);
                    ColumnText.showTextAligned(canvas, settings.getAlign().getElementAlign(), phrase,
                            variables.getPageWidth() + (i - variables.getCenterLine()) * variables.getLineWidth(),
                            variables.getPageHeight() - (i - variables.getCenterLine()) * variables.getLineHeight(),
                            variables.getAngle());
                }
            }
        }

    }


    private PdfContentByte getCanvas(PdfStamper pdfStamper, PdfWatermarkSettings settings, int pageNumber) {
        PdfContentByte canvas;
        if (settings.getLayer() == null) {
            canvas = pdfStamper.getOverContent(pageNumber);
        } else {
            switch (settings.getLayer()) {
                case UNDER:
                    canvas = pdfStamper.getUnderContent(pageNumber);
                    break;
                case OVER:
                default:
                    canvas = pdfStamper.getOverContent(pageNumber);
                    break;
            }
        }
        PdfGState gstate = new PdfGState();
        gstate.setFillOpacity(settings.getOpacity());
        gstate.setStrokeOpacity(settings.getOpacity());
        canvas.setGState(gstate);
        return canvas;

    }

    private void setPageDiagonalLength(InternalWatermarkVariables variables, PdfWatermarkSettings.WatermarkPosition watermarkPosition) {
        double pageDiagonalLength = 0;
        switch (watermarkPosition) {
            case DIAGONAL:
                pageDiagonalLength = Math.sqrt(Math.pow(variables.getPageWidth(), 2) + Math.pow(variables.getPageHeight(), 2));
                break;
            case TOP:
            case BOTTOM:
                pageDiagonalLength = variables.getPageWidth();
                break;
            case LEFT_BOTTOM:
            case LEFT_TOP:
            case RIGHT_TOP:
            case RIGHT_BOTTOM:
                pageDiagonalLength = variables.getPageHeight();
                break;
            default:
                LOGGER.error(LOG_ERROR_WATERMARK_POSITION_NOT_FOUND);
                break;
        }
        pageDiagonalLength *= 2;
        variables.setPageDiagonalLength(pageDiagonalLength);

    }

    private void setPhrases(InternalWatermarkVariables variables, PdfWatermarkSettings settings, List<String> lines) {
        Font defaultFont = new Font(settings.getFontFamily(), settings.getFontSize(), settings.getFontStyle().getStyleCode(), settings.getFontColor());
        BaseFont defaultBaseFont = defaultFont.getCalculatedBaseFont(false);

        for (String line : lines) {
            float baseFontWidth = defaultBaseFont.getWidthPoint(line, settings.getFontSize());
            Font localFont = defaultFont;
            int dec = 0;
            while (variables.getPageDiagonalLength() <= baseFontWidth) {
                dec++;
                localFont = new Font(settings.getFontFamily(), settings.getFontSize() - dec, settings.getFontStyle().getStyleCode(), settings.getFontColor());
                baseFontWidth = defaultBaseFont.getWidthPoint(line, localFont.getSize());
            }
            variables.getPhrases().add(new Phrase(line, localFont));
        }
    }

    private void setCalculatedVariables(InternalWatermarkVariables variables, PdfWatermarkSettings settings) throws PdfException {
        float angle, lineHeight = 0L, lineWidth = 0L;
        int newPageHeight, newPageWidth;

        //if font is underlined, we add additional 'height' for underline
        float fontSizeValue = settings.getFontSize() + (settings.getFontStyle() == PdfWatermarkSettings.FontStyle.UNDERLINE ? (int) Math.ceil((float) settings.getFontSize() / UNDERLINED_FONT_ADDSPACE_DIVSOR) : 0);

        switch (settings.getWatermarkPosition()) {
            case DIAGONAL:
                angle = (float) Math.atan2(variables.getPageHeight(), variables.getPageWidth());
                if (!variables.getPhrases().isEmpty()) {
                    double beta = HALF_PI - angle;
                    lineHeight = (float) (Math.sin(beta) * fontSizeValue);
                    lineWidth = (float) (Math.cos(beta) * fontSizeValue);
                }
                angle = (float) Math.toDegrees(angle);
                break;
            case TOP:
                angle = 0;
                lineHeight = fontSizeValue;
                variables.setPageHeight((variables.getPageHeight() << 1) - (int) variables.getPhrases().get(0).getFont().getSize());
                break;
            case BOTTOM:
                angle = 0;
                lineHeight = fontSizeValue;
                newPageHeight = 0;
                for (Phrase p : variables.getPhrases()) {
                    newPageHeight += p.getFont().getSize();
                }
                variables.setPageHeight(newPageHeight);
                break;
            case LEFT_BOTTOM:
                angle = ANGLE_270;
                lineHeight = fontSizeValue;
                newPageWidth = 0;
                for (Phrase p : variables.getPhrases()) {
                    newPageWidth += p.getFont().getSize();
                }
                variables.setPageWidth(newPageWidth);
                break;
            case RIGHT_BOTTOM:
                angle = ANGLE_270;
                lineHeight = fontSizeValue;
                variables.setPageWidth((variables.getPageWidth() << 1) - (int) variables.getPhrases().get(0).getFont().getSize());
                break;
            case LEFT_TOP:
                angle = ANGLE_90;
                lineHeight = fontSizeValue;
                variables.setPageWidth((int) variables.getPhrases().get(0).getFont().getSize());
                break;
            case RIGHT_TOP:
                angle = ANGLE_90;
                lineHeight = fontSizeValue;
                newPageWidth = variables.getPageWidth();
                newPageWidth = newPageWidth << 1;
                for (Phrase p : variables.getPhrases()) {
                    newPageWidth -= p.getFont().getSize();
                }
                variables.setPageWidth(newPageWidth);
                break;
            default:
                LOGGER.error(LOG_ERROR_WATERMARK_POSITION_NOT_FOUND);
                throw new PdfException(LOG_ERROR_WATERMARK_POSITION_NOT_FOUND);
        }

        int phrasesSize = variables.getPhrases().size();
        if (phrasesSize > 1) {
            variables.setCenterLine((phrasesSize - 1) >> 1);
        }

        variables.setAngle(Math.round(angle));
        variables.setLineHeight(lineHeight);
        variables.setLineWidth(lineWidth);

    }

    /**
     * Returns width and height of the page, checks rotation
     *
     * @param reader
     * @param variables
     */
    private void setDefaultWidthAndHeightFromPage(PdfReader reader, InternalWatermarkVariables variables) {
        int width = (int) reader.getPageSize(variables.getPageNumber()).getWidth() >> 1;
        int height = (int) reader.getPageSize(variables.getPageNumber()).getHeight() >> 1;
        if (reader.getPageRotation(variables.getPageNumber()) % ANGLE_180 != 0) {
            variables.setPageHeight(width);
            variables.setPageWidth(height);
        } else {
            variables.setPageWidth(width);
            variables.setPageHeight(height);
        }

    }

}

class InternalWatermarkVariables {

    private int pageWidth;
    private int pageHeight;
    private int pageNumber;
    private double pageDiagonalLength;
    private int angle;
    private float lineHeight;
    private float lineWidth;
    private float fontSizeValue;
    private float centerLine;
    private List<Phrase> phrases;

    public InternalWatermarkVariables(int pageNumber) {
        this.pageNumber = pageNumber;
        pageWidth = -1;
        pageHeight = -1;
        pageDiagonalLength = 0;
        angle = 0;
        lineHeight = 0L;
        lineWidth = 0L;
        fontSizeValue = 0L;
        centerLine = 0L;

        phrases = new ArrayList<>();
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public double getPageDiagonalLength() {
        return pageDiagonalLength;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageDiagonalLength(double pageDiagonalLength) {
        this.pageDiagonalLength = pageDiagonalLength;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public List<Phrase> getPhrases() {
        return phrases;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public float getFontSizeValue() {
        return fontSizeValue;
    }

    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setFontSizeValue(float fontSizeValue) {
        this.fontSizeValue = fontSizeValue;
    }

    public float getCenterLine() {
        return centerLine;
    }

    public void setCenterLine(float centerLine) {
        this.centerLine = centerLine;
    }

}
