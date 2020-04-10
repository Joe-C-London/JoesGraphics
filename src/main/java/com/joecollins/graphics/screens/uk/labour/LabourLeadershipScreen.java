package com.joecollins.graphics.screens.uk.labour;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.Binding.BindingReceiver;
import com.joecollins.graphics.GenericPanelWithHeaderAndLowerThird;
import com.joecollins.graphics.GenericWindow;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.lowerthird.LowerThird;
import com.joecollins.graphics.components.lowerthird.LowerThirdHeadlineOnly;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.TextListener;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class LabourLeadershipScreen extends JPanel {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0");
  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.0");
  private static final Color LABOUR_COLOR = new Color(0xdc241f);
  private final BarFrame leftFrame;
  private final BarFrame rightFrame;

  public static void main(String[] args) {
    ControlPanel controlPanel =
        new ControlPanel(
            new String[] {"Rebecca Long-Bailey", "Lisa Nandy", "Keir Starmer"},
            new String[] {
              "Rosena Allin-Khan", "Richard Burgon", "Dawn Butler", "Ian Murray", "Angela Rayner"
            });
    LabourLeadershipScreen labourLeadershipScreen = new LabourLeadershipScreen(controlPanel);
    LowerThirdHeadlineOnly lowerThird = lowerThird(controlPanel);
    GenericPanelWithHeaderAndLowerThird<LabourLeadershipScreen> panel =
        new GenericPanelWithHeaderAndLowerThird<>(
            labourLeadershipScreen,
            Binding.fixedBinding("LABOUR LEADERSHIP CONTEST 2020"),
            lowerThird);
    new GenericWindow<>(panel).withControlPanel(controlPanel).setVisible(true);
  }

  public LabourLeadershipScreen(ControlPanel controlPanel) {
    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(1024, 512));
    JPanel center = new JPanel();
    center.setBackground(Color.WHITE);
    center.setBorder(new EmptyBorder(5, 5, 5, 5));
    center.setLayout(new GridLayout(1, 0, 5, 5));
    leftFrame =
        createFrameForCandidates(
            controlPanel.createLeftHeaderBinding(),
            controlPanel.createLeftSubheadBinding(),
            controlPanel.createLeftResultBinding(),
            controlPanel.createLeftTotalBinding());
    rightFrame =
        createFrameForCandidates(
            controlPanel.createRightHeaderBinding(),
            controlPanel.createRightSubheadBinding(),
            controlPanel.createRightResultBinding(),
            controlPanel.createRightTotalBinding());
    center.add(leftFrame);
    center.add(rightFrame);
    add(center, BorderLayout.CENTER);
  }

  private BarFrame createFrameForCandidates(
      Binding<String> header,
      Binding<String> subhead,
      Binding<Map<String, Pair<Integer, Double>>> candidates,
      Binding<Integer> total) {
    BindingReceiver<Integer> totalResult = new BindingReceiver<>(total);
    return BarFrameBuilder.basic(
            candidates,
            Function.identity(),
            s -> LABOUR_COLOR,
            v -> v.getLeft(),
            v -> {
              if (v.getLeft() == 0) {
                return "WAITING...";
              }
              if (v.getRight() == 0) {
                return DECIMAL_FORMAT.format(v.getLeft());
              }
              return DECIMAL_FORMAT.format(v.getLeft())
                  + " ("
                  + PCT_FORMAT.format(v.getRight())
                  + "%)";
            })
        .withHeader(header)
        .withSubhead(subhead)
        .withNotes(Binding.fixedBinding("SOURCE: Labour Party"))
        .withMax(totalResult.getBinding(i -> i * 2 / 3))
        .withTarget(totalResult.getBinding(i -> i / 2 + 1), n -> "50% TO WIN")
        .withBorder(Binding.fixedBinding(LABOUR_COLOR))
        .withSubheadColor(Binding.fixedBinding(LABOUR_COLOR))
        .build();
  }

  private static LowerThirdHeadlineOnly lowerThird(ControlPanel controlPanel) {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setPlaceBinding(Binding.fixedBinding("WESTMINSTER"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Europe/London")));
    lowerThird.setHeadlineBinding(controlPanel.headerBinding());
    lowerThird.setSubheadBinding(controlPanel.subheadBinding());
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(
            LowerThird.createImage("LABOUR LEADERSHIP 2020", Color.WHITE, LABOUR_COLOR)));
    return lowerThird;
  }

  private static class ControlPanel extends JPanel {

    private final TextField headline = new TextField();
    private final TextField subhead = new TextField();
    private final TableModel[] leader;
    private final TableModel[] deputy;
    private final JComboBox<String> left = new JComboBox<>();
    private final JComboBox<String> right = new JComboBox<>();

    ControlPanel(String[] leader, String[] deputy) {
      setLayout(new FlowLayout());
      setPreferredSize(new Dimension(1024, 256));
      headline.setPreferredSize(new Dimension(100, 25));
      subhead.setPreferredSize(new Dimension(100, 25));
      add(headline);
      add(subhead);
      add(left);
      add(right);
      this.leader = new ResultTableModel[leader.length - 1];
      for (int i = 0; i < leader.length - 1; i++) {
        this.leader[i] = new ResultTableModel(leader);
        add(new JTable(this.leader[i]));
        left.addItem("L" + (i + 1));
        right.addItem("L" + (i + 1));
      }
      this.deputy = new ResultTableModel[deputy.length - 1];
      for (int i = 0; i < deputy.length - 1; i++) {
        this.deputy[i] = new ResultTableModel(deputy);
        add(new JTable(this.deputy[i]));
        left.addItem("D" + (i + 1));
        right.addItem("D" + (i + 1));
      }
      left.setSelectedItem("L1");
      right.setSelectedItem("D1");
    }

    public Binding<String> headerBinding() {
      return createBinding(headline);
    }

    public Binding<String> subheadBinding() {
      return createBinding(subhead);
    }

    private static Binding<String> createBinding(TextField field) {
      return new Binding<>() {

        private TextListener textListener;

        @Override
        public String getValue() {
          String text = field.getText();
          return text.isEmpty() ? null : text;
        }

        @Override
        public void bind(Consumer<String> onUpdate) {
          onUpdate.accept(getValue());
          textListener = e -> onUpdate.accept(getValue());
          field.addTextListener(textListener);
        }

        @Override
        public void unbind() {
          field.removeTextListener(textListener);
        }
      };
    }

    public Binding<String> createLeftHeaderBinding() {
      return createHeaderBinding(left);
    }

    public Binding<String> createRightHeaderBinding() {
      return createHeaderBinding(right);
    }

    private Binding<String> createHeaderBinding(JComboBox<String> box) {
      return new Binding<>() {
        private ActionListener actionListener;

        @Override
        public String getValue() {
          String selected = box.getSelectedItem().toString();
          if (selected.startsWith("L")) {
            return "LABOUR LEADER";
          } else if (selected.startsWith("D")) {
            return "LABOUR DEPUTY LEADER";
          } else {
            return null;
          }
        }

        @Override
        public void bind(Consumer<String> onUpdate) {
          actionListener = e -> onUpdate.accept(getValue());
          box.addActionListener(actionListener);
          onUpdate.accept(getValue());
        }

        @Override
        public void unbind() {
          if (actionListener != null) {
            box.removeActionListener(actionListener);
          }
        }
      };
    }

    public Binding<String> createLeftSubheadBinding() {
      return createSubheadBinding(left);
    }

    public Binding<String> createRightSubheadBinding() {
      return createSubheadBinding(right);
    }

    private Binding<String> createSubheadBinding(JComboBox<String> box) {
      return new Binding<>() {
        private ActionListener actionListener;

        @Override
        public String getValue() {
          String selected = box.getSelectedItem().toString();
          return "ROUND " + selected.substring(1);
        }

        @Override
        public void bind(Consumer<String> onUpdate) {
          actionListener = e -> onUpdate.accept(getValue());
          box.addActionListener(actionListener);
          onUpdate.accept(getValue());
        }

        @Override
        public void unbind() {
          if (actionListener != null) {
            box.removeActionListener(actionListener);
          }
        }
      };
    }

    public Binding<Map<String, Pair<Integer, Double>>> createLeftResultBinding() {
      return createResultBinding(left, ControlPanel::getResult);
    }

    public Binding<Map<String, Pair<Integer, Double>>> createRightResultBinding() {
      return createResultBinding(right, ControlPanel::getResult);
    }

    public Binding<Integer> createLeftTotalBinding() {
      return createResultBinding(left, ControlPanel::getTotal);
    }

    public Binding<Integer> createRightTotalBinding() {
      return createResultBinding(right, ControlPanel::getTotal);
    }

    public <T> Binding<T> createResultBinding(JComboBox<String> box, Function<TableModel, T> func) {
      return new Binding<>() {
        private TableModelListener tableListener;
        private ActionListener actionListener;

        @Override
        public T getValue() {
          return func.apply(getSelectedModel(box));
        }

        @Override
        public void bind(Consumer<T> onUpdate) {
          tableListener = e -> onUpdate.accept(getValue());
          for (TableModel tableModel : leader) {
            tableModel.addTableModelListener(tableListener);
          }
          for (TableModel tableModel : deputy) {
            tableModel.addTableModelListener(tableListener);
          }
          actionListener = e -> onUpdate.accept(getValue());
          box.addActionListener(actionListener);
          onUpdate.accept(getValue());
        }

        @Override
        public void unbind() {
          if (actionListener != null) {
            box.removeActionListener(actionListener);
          }
          if (tableListener != null) {
            for (TableModel tableModel : leader) {
              tableModel.removeTableModelListener(tableListener);
            }
            for (TableModel tableModel : deputy) {
              tableModel.removeTableModelListener(tableListener);
            }
          }
        }
      };
    }

    private TableModel getSelectedModel(JComboBox<String> box) {
      String selected = box.getSelectedItem().toString();
      if (selected.startsWith("L")) {
        return leader[Integer.parseInt(selected.substring(1)) - 1];
      } else if (selected.startsWith("D")) {
        return deputy[Integer.parseInt(selected.substring(1)) - 1];
      } else {
        return null;
      }
    }

    private static Map<String, Pair<Integer, Double>> getResult(TableModel tableModel) {
      Map<String, Pair<Integer, Double>> ret = new LinkedHashMap<>();
      for (int i = 0; i < tableModel.getRowCount(); i++) {
        Integer votes = (Integer) tableModel.getValueAt(i, 1);
        Double pct = (Double) tableModel.getValueAt(i, 2);
        if (votes < 0) continue;
        ret.put(tableModel.getValueAt(i, 0).toString().toUpperCase(), ImmutablePair.of(votes, pct));
      }
      return ret;
    }

    private static int getTotal(TableModel tableModel) {
      int totalVotes = 0;
      double totalPct = 0;
      for (int i = 0; i < tableModel.getRowCount(); i++) {
        int votes = (Integer) tableModel.getValueAt(i, 1);
        double pct = (Double) tableModel.getValueAt(i, 2);
        if (votes <= 0 || pct <= 0) continue;
        totalVotes += votes;
        totalPct += pct;
      }
      return totalVotes == 0 ? 0 : (int) (100 * totalVotes / totalPct);
    }
  }

  private static class ResultTableModel extends AbstractTableModel {

    private final String[] candidates;
    private final int[] votes;
    private final double[] pct;

    public ResultTableModel(String... candidates) {
      this.candidates = candidates;
      this.votes = new int[candidates.length];
      this.pct = new double[candidates.length];
    }

    @Override
    public int getRowCount() {
      return candidates.length;
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return "Candidate";
        case 1:
          return "Votes";
        case 2:
          return "Percent";
        default:
          return null;
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return String.class;
        case 1:
          return Integer.class;
        case 2:
          return Double.class;
        default:
          return null;
      }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex != 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return candidates[rowIndex];
        case 1:
          return votes[rowIndex];
        case 2:
          return pct[rowIndex];
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 1:
          votes[rowIndex] = Integer.parseInt(aValue.toString());
          break;
        case 2:
          pct[rowIndex] = Double.parseDouble(aValue.toString());
          break;
      }
      fireTableCellUpdated(rowIndex, columnIndex);
    }
  }
}
