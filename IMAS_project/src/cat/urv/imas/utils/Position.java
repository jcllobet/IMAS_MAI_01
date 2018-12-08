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
public class Position implements Serializable,Comparable {
    private int row;
    private int column;
    
    public Position() {
        this(0, 0);
    }
    
    public Position(Position other) {
        this.row = other.getRow();
        this.column = other.getColumn();
    }

    public Position(int row, int column){
        this.row = row;
        this.column = column;
    }

    public void set(int row, int col) {
        setRow(row);
        setColumn(col);
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

    @Override
    public String toString() {
        return "(" + String.valueOf(row) + ", " + String.valueOf(column) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Position))
            return false;

        Position other = (Position)obj;
        return getRow() == other.getRow() && getColumn() == other.getColumn();
    }

    @Override
    public int compareTo(Object o) {
        if (o == null)
            return -1;
        if (!(o instanceof  Position))
            return -1;
        Position other = (Position)o;
        if (getRow() == other.getRow() && getColumn() == other.getColumn())
            return 0;
        if (getRow() > other.getRow())
            return 1; // TODO
        else
            return -1;
    }
}
