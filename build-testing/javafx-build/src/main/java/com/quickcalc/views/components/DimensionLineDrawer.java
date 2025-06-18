package com.quickcalc.views.components;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.utils.DimensionFormatter;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import com.quickcalc.constants.UIConstants;
import javafx.geometry.BoundingBox;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import javafx.util.Pair;

public class DimensionLineDrawer {

    // Constants for Permanent Bottom Dimension Line (Supports and Beam Ends)
    private static final Color PERMANENT_DIM_LINE_COLOR = Color.DARKCYAN;
    private static final double PERMANENT_DIM_LINE_WIDTH = 1.0;
    private static final double PERMANENT_DIM_LINE_OFFSET_Y_SCREEN = 85.0; // Y offset from beam centerline in screen coords (dropped lower)
    private static final double PERMANENT_DIM_TICK_HEIGHT_SCREEN = 6.0;
    private static final Color PERMANENT_DIM_TEXT_COLOR = Color.DARKCYAN;
    private static final double PERMANENT_DIM_TEXT_OFFSET_Y_SCREEN = -3.0; // Y offset for text from the dimension line
    private static final double PERMANENT_DIM_TEXT_BG_PADDING_SCREEN = 2.0;

    // Constants for Detailed Bottom Dimension Line (All Points of Interest)
    private static final Color DETAILED_DIM_LINE_COLOR = Color.BLUEVIOLET;
    private static final double DETAILED_DIM_LINE_WIDTH = 0.75;
    private static final double DETAILED_DIM_LINE_OFFSET_Y_SCREEN = 55.0; 
    private static final double DETAILED_DIM_TICK_HEIGHT_SCREEN = 6.0;
    private static final Color DETAILED_DIM_TEXT_COLOR = Color.BLUEVIOLET;
    private static final double DETAILED_DIM_TEXT_OFFSET_Y_SCREEN = -3.0;
    private static final double DETAILED_DIM_TEXT_BG_PADDING_SCREEN = 1.0;

    // Constants for Temporary Top Dimension Line (for selected/hovered elements)
    private static final Color TEMP_DIM_LINE_COLOR = Color.ORANGERED;
    private static final double TEMP_DIM_LINE_WIDTH = 0.75;
    private static final double TEMP_DIM_LINE_OFFSET_Y_SCREEN = -85.0; // Y offset from beam centerline (negative for above)
    private static final double TEMP_DIM_TICK_HEIGHT_SCREEN = 6.0;
    private static final Color TEMP_DIM_TEXT_COLOR = Color.ORANGERED;
    private static final double TEMP_DIM_TEXT_OFFSET_Y_SCREEN = -4.0; // Y offset for text from the dimension line (negative for above)
    private static final double TEMP_DIM_TEXT_BG_PADDING_SCREEN = 1.0;

    private final ViewTransform viewTransform;
    private final BeamModel beamModel;
    private final List<ClickableDimensionText> clickableDimensionTexts;

    public DimensionLineDrawer(ViewTransform viewTransform, BeamModel beamModel, List<ClickableDimensionText> clickableDimensionTexts) {
        this.viewTransform = viewTransform;
        this.beamModel = beamModel;
        this.clickableDimensionTexts = clickableDimensionTexts;
    }

    // Dimension drawing methods moved from BeamCanvas.java

    public void drawTemporaryElementDimensions(GraphicsContext gc, InteractiveElement element) {
        if (this.beamModel == null || element == null || this.viewTransform == null) {
            return;
        }

        Object modelElement = element.getModelElement();
        List<Double> elementKeyEngXs = new ArrayList<>();

        if (modelElement instanceof Load) {
            Load load = (Load) modelElement;
            elementKeyEngXs.add(load.getPosition());
            if (load.getType() == Load.Type.DISTRIBUTED) {
                elementKeyEngXs.add(load.getEndPosition());
            }
        } else if (modelElement instanceof Support) {
            Support support = (Support) modelElement;
            elementKeyEngXs.add(support.getPosition());
        } else {
            return; // Unknown element type
        }
        Collections.sort(elementKeyEngXs);
        if (elementKeyEngXs.isEmpty()) return;

        double elementEngX1 = elementKeyEngXs.get(0);
        double elementEngX2 = (elementKeyEngXs.size() > 1) ? elementKeyEngXs.get(1) : elementEngX1;

        TreeSet<Double> boundaryPointsEng = new TreeSet<>();
        boundaryPointsEng.add(0.0);
        this.beamModel.getSupports().forEach(s -> boundaryPointsEng.add(s.getPosition()));
        boundaryPointsEng.add(this.beamModel.getLength());

        Double foundLeftAnchor = boundaryPointsEng.lower(elementEngX1);
        double leftAnchorEngX = (foundLeftAnchor != null) ? foundLeftAnchor : 0.0;

        Double foundRightAnchor = boundaryPointsEng.higher(elementEngX2);
        double rightAnchorEngX = (foundRightAnchor != null) ? foundRightAnchor : this.beamModel.getLength();
        
        gc.setStroke(TEMP_DIM_LINE_COLOR);
        gc.setLineWidth(TEMP_DIM_LINE_WIDTH);
        gc.setFill(TEMP_DIM_TEXT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setLineDashes(UIConstants.DASHED_LINE_PATTERN);

        double beamScreenCenterY = this.viewTransform.engineeringToScreen(0, 0).getY();
        double dimLineScreenY = beamScreenCenterY + TEMP_DIM_LINE_OFFSET_Y_SCREEN;

        if (elementEngX1 > leftAnchorEngX + 1e-3) { 
            this.drawSingleDimensionSegment(gc, element, "temporary_to_left_anchor", leftAnchorEngX, elementEngX1, dimLineScreenY, beamScreenCenterY);
        }

        boolean isDistributedLoad = modelElement instanceof Load && ((Load) modelElement).getType() == Load.Type.DISTRIBUTED;
        if (isDistributedLoad && elementEngX2 > elementEngX1 + 1e-3) {
            this.drawSingleDimensionSegment(gc, element, "temporary_element_length", elementEngX1, elementEngX2, dimLineScreenY, beamScreenCenterY);
        }

        if (rightAnchorEngX > elementEngX2 + 1e-3) {
            this.drawSingleDimensionSegment(gc, element, "temporary_to_right_anchor", elementEngX2, rightAnchorEngX, dimLineScreenY, beamScreenCenterY);
        }
        
        gc.setLineDashes(null);
    }

    private void drawSingleDimensionSegment(GraphicsContext gc, Object modelElement, String dimensionType, double engXStart, double engXEnd, 
                                            double dimLineScreenY, double beamScreenCenterY) {
        
        Point2D segStartScreen = this.viewTransform.engineeringToScreen(engXStart, 0);
        Point2D segEndScreen = this.viewTransform.engineeringToScreen(engXEnd, 0);
        double segmentLengthEng = engXEnd - engXStart;

        if (segmentLengthEng < 1e-3 || Math.abs(segEndScreen.getX() - segStartScreen.getX()) < 1.0) {
            return;
        }

        gc.strokeLine(segStartScreen.getX(), dimLineScreenY, segEndScreen.getX(), dimLineScreenY);

        gc.strokeLine(segStartScreen.getX(), beamScreenCenterY, segStartScreen.getX(), dimLineScreenY - TEMP_DIM_TICK_HEIGHT_SCREEN / 2);
        gc.strokeLine(segStartScreen.getX(), dimLineScreenY - TEMP_DIM_TICK_HEIGHT_SCREEN / 2, segStartScreen.getX(), dimLineScreenY + TEMP_DIM_TICK_HEIGHT_SCREEN / 2);

        gc.strokeLine(segEndScreen.getX(), beamScreenCenterY, segEndScreen.getX(), dimLineScreenY - TEMP_DIM_TICK_HEIGHT_SCREEN / 2);
        gc.strokeLine(segEndScreen.getX(), dimLineScreenY - TEMP_DIM_TICK_HEIGHT_SCREEN / 2, segEndScreen.getX(), dimLineScreenY + TEMP_DIM_TICK_HEIGHT_SCREEN / 2);

        this.drawDimensionText(gc, segStartScreen.getX(), segEndScreen.getX(), dimLineScreenY, segmentLengthEng, TEMP_DIM_TEXT_OFFSET_Y_SCREEN, TEMP_DIM_TEXT_BG_PADDING_SCREEN, TEMP_DIM_TEXT_COLOR, modelElement, dimensionType, engXStart, engXEnd);
    }

    private void drawDimensionText(GraphicsContext gc, double segmentStartScreenX, double segmentEndScreenX, double dimLineScreenY, double segmentLengthEng, double textOffsetY, double bgPadding, Color textColor, Object modelElement, String dimensionType, double anchor1EngX, double anchor2EngX) {
        String text = DimensionFormatter.formatDimension(segmentLengthEng);
        javafx.scene.text.Text tempTextNode = new javafx.scene.text.Text(text);
        tempTextNode.setFont(gc.getFont());
        double textWidth = tempTextNode.getLayoutBounds().getWidth();
        double textHeight = tempTextNode.getLayoutBounds().getHeight(); 

        double segmentMidScreenX = (segmentStartScreenX + segmentEndScreenX) / 2;
        double textBaselineScreenY = dimLineScreenY + textOffsetY; 

        if (Math.abs(segmentEndScreenX - segmentStartScreenX) < textWidth + 2 * bgPadding) {
            return;
        }

        gc.setFill(Color.WHITE); 
        gc.fillRect(segmentMidScreenX - textWidth / 2 - bgPadding,
                    textBaselineScreenY - textHeight - bgPadding, 
                    textWidth + 2 * bgPadding,
                    tempTextNode.getLayoutBounds().getHeight() + 2 * bgPadding);
        
        gc.setFill(textColor);
        gc.setTextBaseline(VPos.BOTTOM); 
        gc.fillText(text, segmentMidScreenX, textBaselineScreenY);

        // Create and store ClickableDimensionText for interactivity
        double boundsMinX = segmentMidScreenX - textWidth / 2 - bgPadding;
        double boundsMinY = textBaselineScreenY - textHeight - bgPadding;
        double boundsWidth = textWidth + 2 * bgPadding;
        double boundsHeight = textHeight + 2 * bgPadding; // textHeight is LayoutBounds().getHeight(), which is what we used for drawing the bg rect
        BoundingBox screenBounds = new BoundingBox(boundsMinX, boundsMinY, boundsWidth, boundsHeight);

        ClickableDimensionText cdt = new ClickableDimensionText(
            screenBounds,
            modelElement,
            dimensionType,
            segmentLengthEng,
            text,
            anchor1EngX,
            anchor2EngX
        );
        this.clickableDimensionTexts.add(cdt);
    }

    public void drawPermanentBottomDimensionLine(GraphicsContext gc) {
        if (this.beamModel == null || this.viewTransform == null || this.beamModel.getSupports().isEmpty() && this.beamModel.getLength() == 0) {
            return;
        }

        gc.setStroke(PERMANENT_DIM_LINE_COLOR);
        gc.setLineWidth(PERMANENT_DIM_LINE_WIDTH);
        gc.setFill(PERMANENT_DIM_TEXT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setLineDashes(null); // Solid line

        double beamScreenCenterY = this.viewTransform.engineeringToScreen(0, 0).getY();
        double dimLineScreenY = beamScreenCenterY + PERMANENT_DIM_LINE_OFFSET_Y_SCREEN;

        TreeSet<Double> tickLocationsEng = new TreeSet<>();
        tickLocationsEng.add(0.0); // Always include beam start
        this.beamModel.getSupports().forEach(s -> tickLocationsEng.add(s.getPosition()));
        tickLocationsEng.add(this.beamModel.getLength()); // Always include beam end

        List<Double> sortedTickLocationsEng = new ArrayList<>(tickLocationsEng);
        if (sortedTickLocationsEng.size() < 2) return;

        double overallStartEngX = sortedTickLocationsEng.get(0);
        double overallEndEngX = sortedTickLocationsEng.get(sortedTickLocationsEng.size() - 1);

        Point2D overallStartScreen = this.viewTransform.engineeringToScreen(overallStartEngX, 0);
        Point2D overallEndScreen = this.viewTransform.engineeringToScreen(overallEndEngX, 0);

        // Draw the main continuous dimension line if there's any length
        if (Math.abs(overallEndScreen.getX() - overallStartScreen.getX()) > 1.0) {
             gc.strokeLine(overallStartScreen.getX(), dimLineScreenY, overallEndScreen.getX(), dimLineScreenY);
        }

        for (int i = 0; i < sortedTickLocationsEng.size(); i++) {
            double engX = sortedTickLocationsEng.get(i);
            Point2D tickScreenPos = this.viewTransform.engineeringToScreen(engX, 0);

            // Draw tick on the dimension line (now short, like detailed ticks)
            gc.strokeLine(tickScreenPos.getX(), dimLineScreenY - PERMANENT_DIM_TICK_HEIGHT_SCREEN / 2, tickScreenPos.getX(), dimLineScreenY + PERMANENT_DIM_TICK_HEIGHT_SCREEN / 2);

            // Draw dimension text between ticks
            if (i < sortedTickLocationsEng.size() - 1) {
                double nextEngX = sortedTickLocationsEng.get(i + 1);
                double segmentLengthEng = nextEngX - engX;
                Point2D nextTickScreenPos = this.viewTransform.engineeringToScreen(nextEngX, 0);
                if (segmentLengthEng > 1e-3 && Math.abs(nextTickScreenPos.getX() - tickScreenPos.getX()) > 1.0) {
                    // Use the existing drawDimensionText helper
                    this.drawDimensionText(gc, tickScreenPos.getX(), nextTickScreenPos.getX(), dimLineScreenY, segmentLengthEng, PERMANENT_DIM_TEXT_OFFSET_Y_SCREEN, PERMANENT_DIM_TEXT_BG_PADDING_SCREEN, PERMANENT_DIM_TEXT_COLOR, this.beamModel, "permanent_spacing", engX, nextEngX);
                }
            }
        }
    }

    public void drawDetailedBottomDimensionLine(GraphicsContext gc) {
        if (this.beamModel == null || this.viewTransform == null) {
            return;
        }

        List<Pair<Double, Double>> permanentDimensionSegments = new ArrayList<>();
        if (!this.beamModel.getSupports().isEmpty()) {
            TreeSet<Double> permTickLocationsEngSet = new TreeSet<>();
            permTickLocationsEngSet.add(0.0);
            this.beamModel.getSupports().forEach(s -> permTickLocationsEngSet.add(s.getPosition()));
            permTickLocationsEngSet.add(this.beamModel.getLength());
            
            List<Double> sortedPermTickLocationsEng = new ArrayList<>(permTickLocationsEngSet);
            if (sortedPermTickLocationsEng.size() >= 2) {
                for (int k = 0; k < sortedPermTickLocationsEng.size() - 1; k++) {
                    double segStart = sortedPermTickLocationsEng.get(k);
                    double segEnd = sortedPermTickLocationsEng.get(k + 1);
                    if (segEnd - segStart > 1e-2) { 
                         permanentDimensionSegments.add(new Pair<>(segStart, segEnd));
                    }
                }
            }
        }

        TreeSet<Double> tickLocationsEng = new TreeSet<>();
        tickLocationsEng.add(0.0);
        tickLocationsEng.add(this.beamModel.getLength());
        this.beamModel.getSupports().forEach(s -> tickLocationsEng.add(s.getPosition()));
        for (Load load : this.beamModel.getLoads()) {
            tickLocationsEng.add(load.getPosition());
            if (load.getType() == Load.Type.DISTRIBUTED) {
                tickLocationsEng.add(load.getEndPosition());
            }
        }

        List<Double> sortedTickLocationsEng = new ArrayList<>(tickLocationsEng);
        if (sortedTickLocationsEng.size() < 2) return;

        gc.setStroke(DETAILED_DIM_LINE_COLOR);
        gc.setLineWidth(DETAILED_DIM_LINE_WIDTH);
        gc.setFill(DETAILED_DIM_TEXT_COLOR);
        gc.setLineDashes(null); // Solid line

        double beamScreenCenterY = this.viewTransform.engineeringToScreen(0, 0).getY();
        double dimLineScreenY = beamScreenCenterY + DETAILED_DIM_LINE_OFFSET_Y_SCREEN;

        for (int i = 0; i < sortedTickLocationsEng.size() - 1; i++) {
            double currentTickEngX = sortedTickLocationsEng.get(i);
            double nextTickEngX = sortedTickLocationsEng.get(i + 1);
            double segmentLengthEng = nextTickEngX - currentTickEngX;

            Point2D currentTickScreen = this.viewTransform.engineeringToScreen(currentTickEngX, 0);
            Point2D nextTickScreen = this.viewTransform.engineeringToScreen(nextTickEngX, 0);
            double segmentScreenWidth = Math.abs(nextTickScreen.getX() - currentTickScreen.getX());

            if (segmentLengthEng > 1e-2 && segmentScreenWidth > 10) {
                boolean isDuplicate = false;
                for (Pair<Double, Double> permSegmentPair : permanentDimensionSegments) {
                    if (Math.abs(permSegmentPair.getKey() - currentTickEngX) < 0.01 &&
                        Math.abs(permSegmentPair.getValue() - nextTickEngX) < 0.01) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                if (!isDuplicate) {
                    gc.strokeLine(currentTickScreen.getX(), dimLineScreenY, nextTickScreen.getX(), dimLineScreenY);
                    gc.strokeLine(currentTickScreen.getX(), dimLineScreenY - DETAILED_DIM_TICK_HEIGHT_SCREEN / 2,
                                  currentTickScreen.getX(), dimLineScreenY + DETAILED_DIM_TICK_HEIGHT_SCREEN / 2);
                    gc.strokeLine(nextTickScreen.getX(), dimLineScreenY - DETAILED_DIM_TICK_HEIGHT_SCREEN / 2,
                                  nextTickScreen.getX(), dimLineScreenY + DETAILED_DIM_TICK_HEIGHT_SCREEN / 2);
                    // Determine modelElement and dimensionType for ClickableDimensionText
                    Object relevantElement = null;
                    String dimType = "detailed_spacing"; // Default

                    // Check if it's a distributed load's length
                    for (Load load : this.beamModel.getLoads()) {
                        if (load.getType() == Load.Type.DISTRIBUTED &&
                            Math.abs(load.getPosition() - currentTickEngX) < 1e-3 &&
                            Math.abs(load.getEndPosition() - nextTickEngX) < 1e-3) {
                            relevantElement = load;
                            dimType = "detailed_load_length";
                            break;
                        }
                    }

                    if (relevantElement == null) {
                        // Check if nextTickEngX is a primary point of an element
                        for (Load load : this.beamModel.getLoads()) {
                            if (Math.abs(load.getPosition() - nextTickEngX) < 1e-3) {
                                relevantElement = load;
                                dimType = "detailed_load_position"; // Segment measures to this load's start
                                break;
                            }
                            if (load.getType() == Load.Type.DISTRIBUTED && Math.abs(load.getEndPosition() - nextTickEngX) < 1e-3) {
                                relevantElement = load;
                                dimType = "detailed_load_end_position"; // Segment measures to this load's end
                                break;
                            }
                        }
                        if (relevantElement == null) {
                            for (Support support : this.beamModel.getSupports()) {
                                if (Math.abs(support.getPosition() - nextTickEngX) < 1e-3) {
                                    relevantElement = support;
                                    dimType = "detailed_support_position"; // Segment measures to this support
                                    break;
                                }
                            }
                        }
                    }

                    // If no specific element association found by looking at nextTickEngX or full span,
                    // assign the beam model itself. This covers gaps, segments from beam start, etc.
                    if (relevantElement == null) {
                        relevantElement = this.beamModel;
                    }

                    this.drawDimensionText(gc, currentTickScreen.getX(), nextTickScreen.getX(), dimLineScreenY, segmentLengthEng, DETAILED_DIM_TEXT_OFFSET_Y_SCREEN, DETAILED_DIM_TEXT_BG_PADDING_SCREEN, DETAILED_DIM_TEXT_COLOR, relevantElement, dimType, currentTickEngX, nextTickEngX);
                }
            }
        }
    }
}
