/*
 * This file is part of the MLDA.
 *
 * (c)  Jose Maria Moyano Murillo
 *      Eva Lucrecia Gibaja Galindo
 *      Sebastian Ventura Soto <sventura@uco.es>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package models;

import javax.swing.table.AbstractTableModel;

/**
 * Abstract Table Model 2
 * 
 * @author Jose Maria Moyano Murillo
 */
public class AbstractTableModel2 extends AbstractTableModel {
    
    Object[][] data;
    Object[] column;
            
    /**
     * Constructor specifying data and columns types
     * 
     * @param data data
     * @param column column types
     */
    public AbstractTableModel2(Object[][] data, Object[] column)
    {
        this.data = data;
        this.column = column;
    }
    
   
    @Override
    public int getColumnCount() {
        return column.length - 1;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        return (String) column[col + 1];
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data[row][col + 1];
    }

    /**
     * Set value at a cell
     * 
     * @param obj value
     * @param row row index
     * @param col column index
     */
    public void setValueAt(double obj, int row, int col) {
        data[row][col + 1] = obj;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }
}
