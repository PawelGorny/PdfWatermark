package com.pawelgorny.pdfwatermark;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Settings for the PdfWatermark service: Position, align of the text, layer...
 */
public class PdfWatermarkSettings {

    public enum Layer {
        OVER, UNDER
    }

    public enum Align {
        ALIGN_CENTER(Element.ALIGN_CENTER), ALIGN_LEFT(Element.ALIGN_LEFT), ALIGN_RIGHT(Element.ALIGN_RIGHT);

        private final int elementAlign;

        Align(int elementAlign) {
            this.elementAlign = elementAlign;
        }

        public int getElementAlign() {
            return elementAlign;
        }

    }

    public enum WatermarkPosition {
        DIAGONAL, TOP, BOTTOM, LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    public enum FontStyle {
        NORMAL(Font.NORMAL), BOLD(Font.BOLD), ITALIC(Font.ITALIC), BOLD_ITALIC(Font.BOLDITALIC), UNDERLINE(Font.UNDERLINE);

        private final int styleCode;

        public static final List<String> STRINGVALUES = Collections.unmodifiableList(Arrays.asList(NORMAL.name(), BOLD.name(), ITALIC.name(), BOLD_ITALIC.name(), UNDERLINE.name()));

        FontStyle(int styleCode) {
            this.styleCode = styleCode;
        }

        public int getStyleCode() {
            return styleCode;
        }

    }

    private FontFamily fontFamily;
    private int fontSize;
    private BaseColor fontColor;
    private FontStyle fontStyle;
    private float opacity;
    private WatermarkPosition watermarkPosition;
    private Layer layer;
    private Align align;
    private boolean firstPageOnly = false;

    private Map<String, String> infos = null;

    private static final int DEFAULT_FONTSIZE = 14;
    private static final float DEFAULT_OPACITY = 0.7F;


    public PdfWatermarkSettings() {
    }

    public PdfWatermarkSettings(boolean defaultInitialise) {
        this();
        if (defaultInitialise) {
            fontFamily = FontFamily.HELVETICA;
            fontSize = DEFAULT_FONTSIZE;
            fontColor = BaseColor.LIGHT_GRAY;
            fontStyle = FontStyle.BOLD;
            align = Align.ALIGN_CENTER;
            opacity = DEFAULT_OPACITY;
            watermarkPosition = WatermarkPosition.DIAGONAL;

        }
    }

    public FontFamily getFontFamily() {
        return fontFamily;
    }

    public int getFontSize() {
        return fontSize;
    }

    public BaseColor getFontColor() {
        return fontColor;
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public float getOpacity() {
        return opacity;
    }

    public WatermarkPosition getWatermarkPosition() {
        return watermarkPosition;
    }

    public void setFontFamily(FontFamily fontFamily) {
        this.fontFamily = fontFamily;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void setFontColor(BaseColor fontColor) {
        this.fontColor = fontColor;
    }

    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setWatermarkPosition(WatermarkPosition watermarkPosition) {
        this.watermarkPosition = watermarkPosition;
    }

    public Layer getLayer() {
        return layer;
    }

    public Align getAlign() {
        return align;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public Map<String, String> getInfos() {
        return infos;
    }

    public void setInfos(Map<String, String> infos) {
        this.infos = infos;
    }

    public boolean isFirstPageOnly() {
        return firstPageOnly;
    }

    public void setFirstPageOnly(boolean firstPageOnly) {
        this.firstPageOnly = firstPageOnly;
    }
}
