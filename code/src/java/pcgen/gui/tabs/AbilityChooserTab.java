/*
 * AbilityChooserTab.java
 * Copyright 2008 Connor Petty <cpmeister@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Created on Jun 29, 2008, 10:30:57 PM
 */
package pcgen.gui.tabs;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import pcgen.gui.UIContext;
import pcgen.gui.facade.AbilityCatagoryFacade;
import pcgen.gui.facade.AbilityFacade;
import pcgen.gui.facade.CharacterFacade;
import pcgen.gui.tools.FilteredTreeViewDisplay;
import pcgen.gui.util.GenericListModel;
import pcgen.gui.util.JTreeTable;
import pcgen.gui.util.JTreeViewPane;
import pcgen.gui.util.event.GenericListDataEvent;
import pcgen.gui.util.event.GenericListDataListener;
import pcgen.gui.util.panes.FlippingSplitPane;
import pcgen.gui.util.treetable.TreeTableModel;
import pcgen.gui.util.treeview.*;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class AbilityChooserTab extends AbstractChooserTab
{

    private static final DataView<AbilityFacade> abilityDataView = new DataView<AbilityFacade>()
    {

        private final List<? extends DataViewColumn> dataColumns =
                Arrays.asList(new DefaultDataViewColumn("Type",
                                                        String.class),
                              new DefaultDataViewColumn("Mult",
                                                        Boolean.class),
                              new DefaultDataViewColumn("Stack",
                                                        Boolean.class),
                              new DefaultDataViewColumn("Description",
                                                        String.class),
                              new DefaultDataViewColumn("Source",
                                                        String.class));

        public List<? extends DataViewColumn> getDataColumns()
        {
            return dataColumns;
        }

        public List<?> getData(AbilityFacade obj)
        {
            return Arrays.asList(getTypes(obj.getTypes()),
                                 obj.isMult(),
                                 obj.isStackable(),
                                 obj.getDescription(),
                                 obj.getSource());
        }

        private String getTypes(List<String> types)
        {
            String ret = types.get(0);
            for (int x = 1; x < types.size(); x++)
            {
                ret += ", " + types.get(x);
            }
            return ret;
        }

    };
    private final FilteredTreeViewDisplay availableTreeViewDisplay;
    private final FilteredTreeViewDisplay selectedTreeViewDisplay;
    private final JTreeTable catagoryTreeTable;

    public AbilityChooserTab(UIContext context)
    {
        super(context);
        this.availableTreeViewDisplay = new FilteredTreeViewDisplay(context);
        this.selectedTreeViewDisplay = new FilteredTreeViewDisplay(context);
        this.catagoryTreeTable = new JTreeTable();
        initComponents();
    }

    private void initComponents()
    {

        selectedTreeViewDisplay.setRowSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        availableTreeViewDisplay.setRowSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableTreeViewDisplay.setTreeViewModel(AbilityFacade.class,
                                                  new AvailableAbilityTreeViewModel());

        FlippingSplitPane pane = new FlippingSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                       true,
                                                       selectedTreeViewDisplay,
                                                       availableTreeViewDisplay);
        pane.setOneTouchExpandable(true);
        pane.setDividerSize(7);
        setPrimaryChooserComponent(pane);

        catagoryTreeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane catagoryScrollPane = new JScrollPane(catagoryTreeTable,
                                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setSecondaryChooserComponent(catagoryScrollPane);
    }

    private final class SelectedAbilityTreeViewModel implements TreeViewModel<AbilityFacade>
    {

        private final List<? extends TreeView<AbilityFacade>> treeViews =
                Arrays.asList(new SelectedAbilityTreeView(AbilityTreeView.NAME),
                              new SelectedAbilityTreeView(AbilityTreeView.TYPE_NAME),
                              new SelectedAbilityTreeView(AbilityTreeView.PREREQ_TREE),
                              new SelectedAbilityTreeView(AbilityTreeView.SOURCE_NAME));
        private final Map<AbilityFacade, AbilityCatagoryFacade> abilityMap;
        private final GenericListModel<AbilityFacade> model;
        private final CharacterFacade character;

        public SelectedAbilityTreeViewModel(CharacterFacade character)
        {
            this.abilityMap = new HashMap<AbilityFacade, AbilityCatagoryFacade>();
            this.model = new GenericListModel<AbilityFacade>();
            this.character = character;

            AbilityCatagoryFacade[] catagories = context.getAbilityCatagories(character).toArray(new AbilityCatagoryFacade[0]);
            for (final AbilityCatagoryFacade catagory : catagories)
            {
                final GenericListModel<AbilityFacade> abilityList = character.getAbilities(catagory);
                AbilityFacade[] abilities = abilityList.toArray(new AbilityFacade[0]);
                for (AbilityFacade ability : abilities)
                {
                    abilityMap.put(ability, catagory);
                }
                abilityList.addGenericListDataListener(
                        new AbilityModelListener()
                        {

                            public void intervalAdded(GenericListDataEvent e)
                            {
                                List<AbilityFacade> sublist =
                                        abilityList.subList(e.getIndex0(),
                                                            e.getIndex1() + 1);
                                for (AbilityFacade ability : sublist)
                                {
                                    abilityMap.put(ability, catagory);
                                }
                                model.addAll(sublist);
                            }

                        });
            }
            model.addAll(abilityMap.keySet());
        }

        public List<? extends TreeView<AbilityFacade>> getTreeViews()
        {
            return treeViews;
        }

        public int getDefaultTreeViewIndex()
        {
            return 0;
        }

        public DataView<AbilityFacade> getDataView()
        {
            return abilityDataView;
        }

        public GenericListModel<AbilityFacade> getDataModel()
        {
            return model;
        }

        private abstract class AbilityModelListener implements GenericListDataListener
        {

            public void intervalRemoved(GenericListDataEvent e)
            {
                model.removeAll(e.getData());
                abilityMap.keySet().removeAll(e.getData());
            }

            public void contentsChanged(GenericListDataEvent e)
            {
                intervalRemoved(e);
                intervalAdded(e);
            }

        }

        private class SelectedAbilityTreeView implements TreeView<AbilityFacade>
        {

            private AbilityTreeView view;

            public SelectedAbilityTreeView(AbilityTreeView view)
            {
                this.view = view;
            }

            public String getViewName()
            {
                return "Catagory/" + view.getViewName();
            }

            public List<TreeViewPath<AbilityFacade>> getPaths(AbilityFacade pobj)
            {
                List<TreeViewPath<AbilityFacade>> paths = new ArrayList<TreeViewPath<AbilityFacade>>();
                for (TreeViewPath<AbilityFacade> path : view.getPaths(pobj))
                {
                    paths.add(path.pathByAddingParent(abilityMap.get(pobj).toString()));
                }
                return paths;
            }

        }
    }

    private static final class AvailableAbilityTreeViewModel implements TreeViewModel<AbilityFacade>,
                                                                           GenericListDataListener
    {

        private final GenericListModel<AbilityFacade> dataModel;
        private GenericListModel<AbilityFacade> data;

        public AvailableAbilityTreeViewModel()
        {
            this.dataModel = new GenericListModel<AbilityFacade>();
        }

        public List<? extends TreeView<AbilityFacade>> getTreeViews()
        {
            return Arrays.asList(AbilityTreeView.values());
        }

        public int getDefaultTreeViewIndex()
        {
            return 0;
        }

        public DataView<AbilityFacade> getDataView()
        {
            return abilityDataView;
        }

        public GenericListModel<AbilityFacade> getDataModel()
        {
            return dataModel;
        }

        public void setSourceModel(GenericListModel<AbilityFacade> data)
        {
            if (this.data != null)
            {
                this.data.removeGenericListDataListener(this);
            }
            synchronized (data)
            {
                dataModel.clear();
                dataModel.addAll(data);
                this.data = data;
                data.addGenericListDataListener(this);
            }
        }

        public void intervalAdded(GenericListDataEvent e)
        {
            dataModel.addAll(e.getIndex0(), data.subList(e.getIndex0(),
                                                         e.getIndex1() + 1));
        }

        public void intervalRemoved(GenericListDataEvent e)
        {
            dataModel.removeRange(e.getIndex0(), e.getIndex1());
        }

        public void contentsChanged(GenericListDataEvent e)
        {
            intervalRemoved(e);
            intervalAdded(e);
        }

    }

    private static final class AbilityCatagoryTreeViewTableModel
            extends TreeViewTableModel<AbilityCatagoryFacade>
    {

        private static final DataViewColumn column = new DefaultDataViewColumn("Remaining",
                                                                                  Integer.class);
        private static final TreeView<AbilityCatagoryFacade> treeview = new TreeView<AbilityCatagoryFacade>()
        {

            public String getViewName()
            {
                return "Catagory";
            }

            public List<TreeViewPath<AbilityCatagoryFacade>> getPaths(AbilityCatagoryFacade pobj)
            {
                TreeViewPath<AbilityCatagoryFacade> path = new TreeViewPath<AbilityCatagoryFacade>(pobj,
                                                                                                   pobj.getType());
                return Collections.singletonList(path);
            }

        };
        private final CharacterFacade character;

        public AbilityCatagoryTreeViewTableModel(final CharacterFacade character,
                                                  GenericListModel<AbilityCatagoryFacade> data)
        {
            super(new DataView<AbilityCatagoryFacade>()
          {

              public List<?> getData(AbilityCatagoryFacade catagory)
              {
                  return Collections.singletonList(character.getRemainingSelections(catagory));
              }

              public List<? extends DataViewColumn> getDataColumns()
              {
                  return Collections.singletonList(column);
              }

          });
            this.character = character;
            setSelectedTreeView(treeview);
            setDataModel(data);
        }

        @Override
        public boolean isCellEditable(Object node, int column)
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            if (treeNode.getUserObject() instanceof AbilityCatagoryFacade)
            {
                return true;
            }
            return super.isCellEditable(node, column);
        }

        @Override
        public void setValueAt(Object aValue, Object node, int column)
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            character.setRemainingSelection((AbilityCatagoryFacade) treeNode.getUserObject(),
                                            (Integer) aValue);
        }

    }

    private final class AbilityTransferHandler extends TransferHandler
    {

        private CharacterFacade character;

        public AbilityTransferHandler(CharacterFacade character)
        {
            this.character = character;
        }

        private final DataFlavor abilityFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                                                                  ";class=" +
                                                                  AbilityFacade.class.getName(),
                                                                  null);

        @Override
        public int getSourceActions(JComponent c)
        {
            if (selectedTreeViewDisplay.isAncestorOf(c))
            {
                List<Object> data = selectedTreeViewDisplay.getSelectedData();
                if (data.isEmpty() || data.get(0).getClass() !=
                        AbilityFacade.class)
                {
                    return NONE;
                }
                return MOVE;
            }
            else
            {
                List<Object> data = availableTreeViewDisplay.getSelectedData();
                if (data.isEmpty() || data.get(0).getClass() !=
                        AbilityFacade.class)
                {
                    return NONE;
                }
                AbilityFacade ability = (AbilityFacade) data.get(0);
                if (ability.isMult())
                {
                    return COPY;
                }
                else
                {
                    return MOVE;
                }
            }

        }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            JTreeViewPane pane = (JTreeViewPane) c.getParent();
            final AbilityFacade selectedAbility = (AbilityFacade) pane.getSelectedData().get(0);
            return new Transferable()
            {

                public DataFlavor[] getTransferDataFlavors()
                {
                    return new DataFlavor[]{abilityFlavor};
                }

                public boolean isDataFlavorSupported(DataFlavor flavor)
                {
                    return abilityFlavor == flavor;
                }

                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
                {
                    if (!isDataFlavorSupported(flavor))
                    {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return selectedAbility;
                }

            };
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
        {
            return transferFlavors[0] == abilityFlavor;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t)
        {
            if (selectedTreeViewDisplay.isAncestorOf(comp))
            {
                try
                {
                    AbilityFacade ability = (AbilityFacade) t.getTransferData(abilityFlavor);
                    // TODO: add some extra logic
                    character.addAbility(getSelectedCatagory(), ability);
                    return true;
                }
                catch (UnsupportedFlavorException ex)
                {
                    Logger.getLogger(AbilityChooserTab.class.getName()).log(Level.SEVERE,
                                                                            null,
                                                                            ex);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(AbilityChooserTab.class.getName()).log(Level.SEVERE,
                                                                            null,
                                                                            ex);
                }
                return false;
            }
            return true;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data,
                                   int action)
        {
            if (action == COPY)
            {
                return;
            }
        }

    }

    public void setSelectedCatagory(AbilityCatagoryFacade catagory)
    {

    }

    public AbilityCatagoryFacade getSelectedCatagory()
    {
        return null;
    }

    public Hashtable<Object, Object> createState(CharacterFacade character)
    {
        Hashtable<Object, Object> state = new Hashtable<Object, Object>();
        state.put("CatagoryModel",
                  new AbilityCatagoryTreeViewTableModel(character,
                                                        context.getAbilityCatagories(character)));
        state.put("SelectedModel", new SelectedAbilityTreeViewModel(character));

        return state;
    }

    public void storeState(Hashtable<Object, Object> state)
    {
        state.put("SelectedCatagory", catagoryTreeTable.getSelectedRow());
    }

    public void restoreState(Hashtable<?, ?> state)
    {
        catagoryTreeTable.setTreeTableModel((TreeTableModel) state.get("CatagoryModel"));
        int selectedCatatoryIndex = (Integer) state.get("SelectedCatagory");
        catagoryTreeTable.getSelectionModel().setSelectionInterval(selectedCatatoryIndex,
                                                                   selectedCatatoryIndex);
        selectedTreeViewDisplay.setTreeViewModel(AbilityFacade.class,
                                                 (SelectedAbilityTreeViewModel) state.get("SelectedModel"));

    }

    private static enum AbilityTreeView implements TreeView<AbilityFacade>
    {

        NAME("Name"),
        TYPE_NAME("Type/Name"),
        PREREQ_TREE("Prereq Tree"),
        SOURCE_NAME("Source/Name");
        private String name;

        private AbilityTreeView(String name)
        {
            this.name = name;
        }

        public String getViewName()
        {
            return name;
        }

        public List<TreeViewPath<AbilityFacade>> getPaths(AbilityFacade pobj)
        {
            switch (this)
            {
                case NAME:
                    return Collections.singletonList(new TreeViewPath<AbilityFacade>(pobj));
                case TYPE_NAME:
                    List<TreeViewPath<AbilityFacade>> list = new ArrayList<TreeViewPath<AbilityFacade>>();
                    for (String type : pobj.getTypes())
                    {
                        list.add(new TreeViewPath<AbilityFacade>(pobj, type));
                    }
                    return list;
                case PREREQ_TREE:
                    return null;
                case SOURCE_NAME:
                    return Collections.singletonList(new TreeViewPath<AbilityFacade>(pobj,
                                                                                     pobj.getSource()));
                default:
                    throw new InternalError();
            }
        }

    }

    private class PreReqTreeView implements TreeView<AbilityFacade>
    {

        private GenericListModel<AbilityFacade> abilities;

        public PreReqTreeView(GenericListModel<AbilityFacade> abilities)
        {
            this.abilities = abilities;
        }

        public String getViewName()
        {
            return "Prereq Tree";
        }

        public List<TreeViewPath<AbilityFacade>> getPaths(AbilityFacade pobj)
        {
            List<List<AbilityFacade>> abilityPaths = new ArrayList<List<AbilityFacade>>();
            addPaths(abilityPaths, pobj.getRequiredAbilities(),
                     new ArrayList<AbilityFacade>());
            if (abilityPaths.isEmpty())
            {
                return Collections.singletonList(new TreeViewPath<AbilityFacade>(pobj));
            }

            List<TreeViewPath<AbilityFacade>> paths = new ArrayList<TreeViewPath<AbilityFacade>>();
            for (List<AbilityFacade> path : abilityPaths)
            {
                Collections.reverse(path);
                Object[] array = path.toArray();
                for (int x = 0; x < array.length; x++)
                {
                    if (!abilities.contains(array[x]))
                    {
                        array[x] = array[x].toString();
                    }
                }
                paths.add(new TreeViewPath<AbilityFacade>(array, pobj));
            }
            return paths;
        }

        private void addPaths(List<List<AbilityFacade>> abilityPaths,
                               List<AbilityFacade> preAbilities,
                               ArrayList<AbilityFacade> path)
        {
            for (AbilityFacade preAbility : preAbilities)
            {
                @SuppressWarnings("unchecked")
                ArrayList<AbilityFacade> pathclone = (ArrayList<AbilityFacade>) path.clone();
                pathclone.add(preAbility);
                List<AbilityFacade> preAbilities2 = preAbility.getRequiredAbilities();
                if (preAbilities2.isEmpty())
                {
                    abilityPaths.add(pathclone);
                }
                else
                {
                    addPaths(abilityPaths, preAbilities2, pathclone);
                }
            }
        }

    }
}
