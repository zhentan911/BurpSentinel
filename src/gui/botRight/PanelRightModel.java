/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui.botRight;

import gui.categorizer.CategorizerManager;
import gui.categorizer.model.ResponseCategory;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;
import model.SentinelHttpMessageAtk;
import util.SettingsManager;
import util.Utility;

/**
 *
 * Observes:
 * - httpMessages
 * - Categorizer
 * 
 * @author Dobin
 */
public class PanelRightModel extends AbstractTableModel implements Observer {
    private LinkedList<SentinelHttpMessageAtk> messages;
    private PanelRightUi parent;
    
    public PanelRightModel(PanelRightUi parent) {
        this.parent = parent;
        messages = new LinkedList<SentinelHttpMessageAtk>();
        CategorizerManager.getInstance().addObserver(this);
    }
    
    /*
     * We observe 2 things:
     * - httpmessages 
     *   - update httpmessage if orighttpmsg changes (size etc)
     * - categorizer
     *   - used to update the httpmessages if categories change
     */
    @Override
    public void update(Observable o, Object arg) {
        // We dont know what changed FIXME
        if (o.getClass().equals(CategorizerManager.class)) {
            this.fireTableDataChanged();
        } else {
            this.fireTableDataChanged();
        }
    }
    
    @Override
    public int getRowCount() {
        return messages.size();
    }

    @Override
    public int getColumnCount() {
        return 12;
    }
    
        
    @Override
    public Class getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0: return Integer.class;
            case 5: return Integer.class;
            case 6: return Integer.class;
            case 7: return Integer.class;
            case 8: return Integer.class;
            default: return String.class;
        }
    }
    
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "#";
            case 1:
                return "Type";
            case 2:
                return "Name";
            case 3:
                return "Original";
            case 4:
                return "Attack";
                
                
            case 5:
                return "Status";
            case 6:
                return "Length";
            case 7:
                return "#TAGS";
            case 8:
                return "Time";
                
            case 9:
                return "Test";
            case 10:
                return "R";
            case 11:
                return "Info";
                
            default:
                return "hmm";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SentinelHttpMessageAtk m = messages.get(rowIndex);
        
        switch(columnIndex) {
            case 0: return rowIndex;
            case 1: return m.getReq().getChangeParam().getTypeStr();
            case 2: return m.getReq().getChangeParam().getName();
            case 3: return m.getReq().getOrigParam().getDecodedValue();
            case 4: return m.getReq().getChangeParam().getDecodedValue();
            case 5: return m.getRes().getHttpCode();
            case 6: return getValueResponseSize(m);
            case 7: return m.getRes().getDomCount();
            case 8: return (int) m.getLoadTime();
            case 9: return getValueAttackResult(m);
            case 10: return getValueResult(m);
            case 11: return getValueCategorizer(m);
            default: return "AAA";
        }
    }
    
    public String getTooltipAt(int rowIndex, int columnIndex) {
        String ret = null;
        
        if (rowIndex >= messages.size()) {
            return null;
        }
        SentinelHttpMessageAtk m = messages.get(rowIndex);
        
        switch(columnIndex) {
            case 2:
                ret = getTooltipOriginalName(m);
                break;
            case 3:
                ret = getTooltipOriginalValue(m);
                break;
            case 4:
                ret = getTooltipAttack(m);
                break;
            case 10: 
                ret = getTooltipAttackResult(m);
                break;
            case 11: 
                ret = getTooltipCategorizer(m);
                break;
        }
        
        return ret;
    }
    
    
    /*** Tooltips ***/
    
    private String getTooltipOriginalName(SentinelHttpMessageAtk m) {
        return m.getReq().getOrigParam().getName();
    }
    
    private String getTooltipOriginalValue(SentinelHttpMessageAtk m) {
        return m.getReq().getOrigParam().getDecodedValue();
    }
    
    private String getTooltipAttack(SentinelHttpMessageAtk m) {
        return m.getReq().getChangeParam().getDecodedValue();
    }
    
    private String getTooltipAttackResult(SentinelHttpMessageAtk m) {
        return m.getAttackResult().getResultDescription();
    }
        
    private String getTooltipCategorizer(SentinelHttpMessageAtk m) {
        String ret = "<html>";
        
        for (ResponseCategory resCategory : m.getRes().getCategories()) {
            ret += resCategory.getIndicator() + "<br>\n";
        }
        
        ret += "</html>";

        return ret;
    }
    
    
    /*** helpers ***/
    
    private String getValueResponseSize(SentinelHttpMessageAtk m) {
        String r = "";
        int size = 0;
        
        if (SettingsManager.getEnableRelativeResponseSize()) {
            size = m.getRes().getSize() - m.getParentHttpMessage().getRes().getSize();
            r = Integer.toString(size);
            if (size > 0) {
                r = "+" + r;
            } else if (size == 0) {
                r = "+0";
            }            
        } else {
            r = Integer.toString(m.getRes().getSize());
        }
        return r;
    }
    
    private String getValueAttackResult(SentinelHttpMessageAtk m) {
        if (m.getAttackResult() != null) {
            return m.getAttackResult().getAttackName();
        } else {
            return "Null";
        }
    }

    private String getValueResult(SentinelHttpMessageAtk m) {
        if (m.getAttackResult() != null) {
            boolean successful = m.getAttackResult().isSuccess();
            if (successful) {
                String ret = "";
                switch (m.getAttackResult().getAttackType()) {
                    case INFO:
                        ret = "<html><b><font color=\"orange\">\u2620</font></b></html>";
                        break;
                    case NONE:
                        break;
                    case VULN:
                        ret = "<html><b><font color=\"red\">\u2620</font></b></html>";
                        break;
                    case ABORT:
                        ret = "<html><b><font color=\"blue\">\u2139</font></b></html>";
                        break;
                }
                return ret;
                //return m.getAttackResult().getAttackType() + "\u26A0";
            } else {
                return "-";
            }
        } else {
            return "Null";
        }
    }
    
    private String getValueCategorizer(SentinelHttpMessageAtk m) {
        StringBuilder res = new StringBuilder("<html>");
        for (ResponseCategory resCategory : m.getRes().getCategories()) {
            res.append("<font color=\"");
            res.append(Utility.ColorToHtmlString(resCategory.getCategoryEntry().getColor()));
            res.append("\">");
            res.append(resCategory.getCategoryEntry().getTag());
            res.append(" </font>");
        }
        res.append("</html>");
        return res.toString();
    }
    
    void addMessage(SentinelHttpMessageAtk httpMessage) {
        messages.add(httpMessage);
        httpMessage.setTableIndexAttack(messages.size() - 1);
        httpMessage.addObserver(this);
        httpMessage.getParentHttpMessage().addObserver(this);
        
        // last entry changed
        this.fireTableRowsInserted(messages.size()-1, messages.size()-1);
    }

    public SentinelHttpMessageAtk getHttpMessage(int n) {
        return messages.get(n);
    }
    
    public LinkedList<SentinelHttpMessageAtk> getAllAttackMessages() {
        return (LinkedList<SentinelHttpMessageAtk>) messages.clone();
    }

    
}
