/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils;

import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author alarca_94
 */
public class Position implements Serializable{
    private int row;
    private int column;
    
    public Position(){
        
    }
    
    public Position(int row, int column){
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
    
    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
