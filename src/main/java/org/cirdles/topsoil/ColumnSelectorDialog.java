/*
 * Copyright 2014 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.topsoil;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.cirdles.topsoil.chart.concordia.ConcordiaChart;
import org.cirdles.topsoil.chart.concordia.ErrorChartToolBar;
import org.cirdles.topsoil.chart.concordia.RecordToErrorEllipseConverter;
import org.cirdles.topsoil.table.Field;
import org.cirdles.topsoil.table.NumberField;
import org.cirdles.topsoil.table.Record;
import org.cirdles.topsoil.table.RecordTableColumn;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.dialog.Dialog;

public class ColumnSelectorDialog extends Dialog {

    private static final String MASTHEAD_TEXT = "Select the column for each variable.";
    private static final String ACTION_NAME = "Create chart";

    public ColumnSelectorDialog(TableView<Record> tableToReadArg) {
        super(null, null);

        setContent(new ColumnSelectorView(tableToReadArg));
        getActions().addAll(new ColumnSelectorAction(tableToReadArg), Dialog.Actions.CANCEL);

        setResizable(false);
        setMasthead(MASTHEAD_TEXT);
    }

    /**
     * This UI element is used by the user to choose which column in the main table determine which value of an ellipse.
     */
    private class ColumnSelectorView extends GridPane {

        private static final String X_LABEL_TEXT = "x";
        private static final String SIGMA_X_LABEL_TEXT = "\u03c3x";
        private static final String Y_LABEL_TEXT = "y";
        private static final String SIGMA_Y_LABEL_TEXT = "\u03c3y";
        private static final String RHO_LABEL_TEXT = "\u03c1";

        private final ChoiceBox<Field<Number>> choiceBoxX;
        private final ChoiceBox<Field<Number>> choiceBoxSigmaX;
        private final ChoiceBox<Field<Number>> choiceBoxY;
        private final ChoiceBox<Field<Number>> choiceBoxSigmaY;
        private final ChoiceBox<Field<Number>> choiceBoxRho;

        public ColumnSelectorView(TableView<Record> table) {
            setAlignment(Pos.CENTER);
            setHgap(12);

            // Make the labels right align.
            ColumnConstraints labelConstraints = new ColumnConstraints();
            labelConstraints.setHalignment(HPos.RIGHT);
            getColumnConstraints().add(labelConstraints);

            List<Field<Number>> fields = new ArrayList<>(table.getColumns().size());
            for (TableColumn<Record, ?> column : table.getColumns()) {
                // Only add Field<Number>s from RecordTableColumns to fields.
                if (column instanceof RecordTableColumn) {
                    RecordTableColumn recordColumn = (RecordTableColumn) column;

                    if (recordColumn.getField() instanceof NumberField) {
                        fields.add(recordColumn.getField());
                    }
                }
            }

            choiceBoxX = createChoiceBox(fields, 0);
            choiceBoxSigmaX = createChoiceBox(fields, 1);
            choiceBoxY = createChoiceBox(fields, 2);
            choiceBoxSigmaY = createChoiceBox(fields, 3);
            choiceBoxRho = createChoiceBox(fields, 4);

            addRow(0, createLabelForNode(choiceBoxX, X_LABEL_TEXT), choiceBoxX);
            addRow(1, createLabelForNode(choiceBoxSigmaX, SIGMA_X_LABEL_TEXT), choiceBoxSigmaX);
            addRow(2, createLabelForNode(choiceBoxY, Y_LABEL_TEXT), choiceBoxY);
            addRow(3, createLabelForNode(choiceBoxSigmaY, SIGMA_Y_LABEL_TEXT), choiceBoxSigmaY);
            addRow(4, createLabelForNode(choiceBoxRho, RHO_LABEL_TEXT), choiceBoxRho);
        }

        public Field<Number> getXSelection() {
            return getSelection(choiceBoxX);
        }

        public Field<Number> getSigmaXSelection() {
            return getSelection(choiceBoxSigmaX);
        }

        public Field<Number> getYSelection() {
            return getSelection(choiceBoxY);
        }

        public Field<Number> getSigmaYSelection() {
            return getSelection(choiceBoxSigmaY);
        }

        public Field<Number> getRhoSelection() {
            return getSelection(choiceBoxRho);
        }

        private <T> T getSelection(ChoiceBox<T> choiceBox) {
            return choiceBox.getSelectionModel().getSelectedItem();
        }
    }

    private class ColumnSelectorAction extends AbstractAction {

        private final TableView<Record> table;

        public ColumnSelectorAction(TableView<Record> table) {
            super(ACTION_NAME);
            this.table = table;
        }

        @Override
        public void execute(ActionEvent ae) {
            hide();

            ColumnSelectorView columnSelector = (ColumnSelectorView) getContent();
            RecordToErrorEllipseConverter converter
                    = new RecordToErrorEllipseConverter(columnSelector.getXSelection(), columnSelector.getSigmaXSelection(),
                                                        columnSelector.getYSelection(), columnSelector.getSigmaYSelection(),
                                                        columnSelector.getRhoSelection());

            Series<Number, Number> series = new Series<>();

            for (Record record : table.getItems()) {
                series.getData().add(new Data<>(0, 0, record));
            }

            ConcordiaChart chart = new ConcordiaChart(converter);
            chart.getData().add(series);
            VBox.setVgrow(chart, Priority.ALWAYS);

            ToolBar toolBar = new ErrorChartToolBar(chart);

            Scene scene = new Scene(new VBox(toolBar, chart), 1200, 800);
            Stage chartStage = new Stage();
            chartStage.setScene(scene);
            chartStage.show();
        }
    }
    
    /*
     * Utility methods for ColumnSelectorView
     */

    /**
     * Create a <code>ChoiceBox</code> with the right parameters
     */
    private static ChoiceBox<Field<Number>> createChoiceBox(List<Field<Number>> fields, int initialSelection) {
        ChoiceBox<Field<Number>> choicebox = new ChoiceBox<>();
        choicebox.getItems().addAll(fields);
        choicebox.getSelectionModel().select(initialSelection);
        choicebox.setMinWidth(300);

        choicebox.setConverter(new StringConverter<Field<Number>>() {

            /*
             * Converts a field to a <code>String</code> by returning its name with all newlines replaced by spaces.
             */
            @Override
            public String toString(Field field) {
                return field.getName().replaceAll("\n", " ");
            }

            /*
             * Instead of converting from a String to a Field as it should as a
             * StringConverter method, this method always returns null, since this method is
             * never used by the ChoiceBox and cannot be implemented deterministically (field names are not
             * unique).
             */
            @Override
            public Field fromString(String string) {
                return null;
            }
        });

        return choicebox;
    }

    /**
     * Returns a new <code>Node</code> containing the argument <code>Node</code> and a new <code>Label</code> with the
     * given text.
     *
     * @param node
     * @param text
     * @return
     */
    private static Label createLabelForNode(Node node, String text) {
        Label label = new Label(text);
        label.setLabelFor(node);

        return label;
    }
}
