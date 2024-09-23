import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import javalib.worldimages.CircleImage;
import javalib.worldimages.EquilateralTriangleImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;
import tester.Tester;

class Cell {
  boolean mine;
  boolean flagged;
  boolean visible;
  ArrayList<Cell> neighbors;

  Cell() {
    this(false, new ArrayList<Cell>());
  }

  Cell(boolean mine, ArrayList<Cell> neighbors) {
    this(mine, false, false, neighbors);
  }

  Cell(boolean mine, boolean flagged, boolean visible, ArrayList<Cell> neighbors) {
    this.mine = mine;
    this.flagged = flagged;
    this.visible = visible;
    this.neighbors = neighbors;
  }

  // Returns the number of mines that are neighbors this Cell
  int countMines() {
    int mineCountIndex = 0;
    for (Cell c : this.neighbors) {
      if (c.mine) {
        mineCountIndex++;
      }
    }
    return mineCountIndex;
  }

  // EFFECT: Updates the neighbors of this Cell by adding itself to their
  // neighbors
  void updateNeighbors() {
    for (Cell c : neighbors) {
      c.neighbors.add(this);
    }
  }

  // Checks if this cell is the same as the given cell
  boolean sameCell(Cell c1, Cell c2) {
    return c1.mine == c2.mine && c1.flagged == c2.flagged && c1.visible == c2.visible;
  }

  // draws a Cell in the Minesweeper game
  public WorldImage draw() {
    // covered cell image
    WorldImage topBox = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.gray));
    // uncovered cell image
    WorldImage box = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
            Color.LIGHT_GRAY));
    // mine image
    WorldImage drawMine = new CircleImage(IUtils.TILE_SIZE / 3, OutlineMode.SOLID, Color.red);
    // flag image
    WorldImage drawFlag = new EquilateralTriangleImage(IUtils.TILE_SIZE / 2, OutlineMode.SOLID,
        Color.cyan);
    // draws covered cells -- will display flag (if any)
    if (!this.visible) {
      if (this.flagged) {
        return new OverlayImage(drawFlag, topBox);
      }
      else {
        return topBox;
      }
    }
    // draws uncovered cells -- will display mine (if any) or neighbors with mines
    // (if any)
    else {
      if (this.mine) {
        return new OverlayImage(drawMine, box);
      }
      else if (this.countMines() > 0) {
        return new OverlayImage(
            new TextImage(Integer.toString(this.countMines()), IUtils.TILE_SIZE / 2, Color.black),
            box);
      }
      else {
        // loops through this revealed cell (which has no adjacent mines) neighbors,
        // reveals them if
        // they are not flagged
        for (int i = 0; i < this.neighbors.size(); i++) {
          if (!neighbors.get(i).flagged) {
            neighbors.get(i).visible = true;
          }
        }
        return box;
      }
    }
  }
}

class ExampleCells {
  
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  
  void reset() {
    this.cell1 = new Cell(false, false, true, new ArrayList<Cell>());
    this.cell2 = new Cell(false, false, true, new ArrayList<Cell>(Arrays.asList(this.cell1)));
    this.cell3 = new Cell(true, true, false, new ArrayList<Cell>(Arrays.asList(this.cell2)));
    this.cell4 = new Cell(false, false, true, new ArrayList<Cell>(Arrays.asList(this.cell3)));
    this.cell5 = new Cell(true, false, true, new ArrayList<Cell>(Arrays.asList(this.cell4)));
  }
  
  // Updates the neighbors of cell examples
  void connectCells() {
    this.reset();
    this.cell1.updateNeighbors();
    this.cell2.updateNeighbors();
    this.cell3.updateNeighbors();
    this.cell4.updateNeighbors();
    this.cell5.updateNeighbors();
  }
  

  // Method for testing updateNeighbor in class Cell
  void testUpdateNeighbors(Tester t) {
    this.reset();
    t.checkExpect(this.cell1.neighbors, new ArrayList<Cell>());
    t.checkExpect(this.cell2.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell1)));
    t.checkExpect(this.cell3.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell2)));
    t.checkExpect(this.cell4.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell3)));
    t.checkExpect(this.cell5.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell4)));
    this.connectCells();
    t.checkExpect(this.cell1.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell2)));
    t.checkExpect(this.cell2.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell3)));
    t.checkExpect(this.cell3.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell2, this.cell4)));
    t.checkExpect(this.cell4.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell3, this.cell5)));
    t.checkExpect(this.cell5.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell4)));
  }

  // Method for testing countMines in class Cell
  void testCountMines(Tester t) {
    this.reset();
    this.connectCells();
    t.checkExpect(this.cell1.countMines(), 0);
    t.checkExpect(this.cell2.countMines(), 1);
    t.checkExpect(this.cell3.countMines(), 0);
    t.checkExpect(this.cell4.countMines(), 2);
    t.checkExpect(this.cell5.countMines(), 0);
  }

  // tests the method draw() in the class Cell
  void testDrawCells(Tester t) {
    this.reset();
    this.connectCells();
    WorldImage box = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
            Color.LIGHT_GRAY));
    WorldImage topBox = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    t.checkExpect(this.cell1.draw(), box);
    t.checkExpect(this.cell2.draw(),
        new OverlayImage(new TextImage("1", IUtils.TILE_SIZE / 2, Color.black), box));
    t.checkExpect(this.cell3.draw(), new OverlayImage(
        new EquilateralTriangleImage(IUtils.TILE_SIZE / 2, OutlineMode.SOLID, Color.cyan), topBox));
    t.checkExpect(this.cell4.draw(),
        new OverlayImage(new TextImage("2", IUtils.TILE_SIZE / 2, Color.black), box));
    t.checkExpect(this.cell5.draw(),
        new OverlayImage(new CircleImage(IUtils.TILE_SIZE / 3, OutlineMode.SOLID, Color.red), box));
  }
}