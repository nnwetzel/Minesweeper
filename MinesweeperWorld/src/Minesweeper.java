import java.util.ArrayList;
import java.util.Arrays;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

interface IUtils {
  int TILE_SIZE = 30;
}

// Represents the Minesweeper game
class MinesweeperWorld extends World {
  Random rand;
  int width;
  int height;
  int mineCount;
  ArrayList<ArrayList<Cell>> grid;
  int gameWidth;
  int gameHeight;
  int revealed;

  // for testing -- seeded random mine placement
  MinesweeperWorld(Random rand, int width, int height, int mineCount) {
    this.rand = rand;
    this.width = width;
    this.height = height;
    this.mineCount = mineCount;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.gameWidth = width * IUtils.TILE_SIZE;
    this.gameHeight = height * IUtils.TILE_SIZE;
    this.initBoard();
    this.generateMines();
    this.createNeighbors();

  }

  // for testing -- sets each cell to be visible
  MinesweeperWorld(Random rand, int width, int height, int mineCount, boolean allVisible) {
    this.rand = rand;
    this.width = width;
    this.height = height;
    this.mineCount = mineCount;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.gameWidth = width * IUtils.TILE_SIZE;
    this.gameHeight = height * IUtils.TILE_SIZE;
    this.initBoard();
    this.generateMines();
    this.createNeighbors();

    // sets each cell to be visible if allVisible is true
    if (allVisible) {
      // loops through 2D ArrayLIst
      for (ArrayList<Cell> r : this.grid) {
        // loops through cells
        for (Cell c : r) {
          c.visible = true;
        }
      }
    }
  }

  // constructor for playing game
  MinesweeperWorld(int width, int height, int mineCount) {
    this(new Random(), width, height, mineCount);
  }

  // EFFECT: Initializes Minesweeper board
  void initBoard() {
    // loops to create 2D Arraylist
    for (int i = 0; i < this.height; i++) {
      grid.add(new ArrayList<Cell>());
      ArrayList<Cell> row = this.grid.get(i);
      // loops to create cells
      for (int j = 0; j < this.width; j++) {
        Cell c = new Cell();
        row.add(c);
      }
    }
  }

  // EFFECT: generates mines at random cells
  void generateMines() {
    int mineCountIndex = this.mineCount;
    // places a mine if this random cell does not already have a mine until the
    // desired mine count is reached
    while (mineCountIndex > 0) {
      Cell tile = this.grid.get(this.rand.nextInt(this.height)).get(this.rand.nextInt(this.width));
      if (!(tile.mine)) {
        tile.mine = true;
        mineCountIndex--;
      }
    }
  }

  // Reveals all mines
  public void revealMines() {
    for (ArrayList<Cell> r : grid) {
      for (Cell c : r) {

        if (c.mine) {
          c.visible = true;
        }
      }
    }
  }

  // Draws the current state of the game
  public WorldScene makeScene() {
    WorldScene board = new WorldScene(this.gameWidth, this.gameHeight);
    WorldImage squares = new EmptyImage();
    // loops through 2D ArrayList to draw columns
    for (int i = 0; i < this.grid.size(); i++) {
      WorldImage drawRow = new EmptyImage();
      ArrayList<Cell> row = this.grid.get(i);
      // loops through cells to draw rows
      for (int j = 0; j < row.size(); j++) {
        drawRow = new BesideImage(drawRow, row.get(j).draw());
      }
      squares = new AboveImage(squares, drawRow);
    }
    board.placeImageXY(squares, gameWidth / 2, gameHeight / 2);
    return board;
  }

  // EFFECT: loops through each cell to update its neighbors
  void createNeighbors() {
    for (int i = 0; i < this.grid.size(); i++) {
      ArrayList<Cell> row = this.grid.get(i);
      int j = 0;
      for (Cell c : row) {
        this.updateNeighbors(c, i, j);
        j++;
      }
    }
  }

  // EFFECT: if this cell is within one x or y of the given Cell, adds itself to
  // the given cell's list of neighbors
  void updateNeighbors(Cell c, int x, int y) {
    // loops through 2D ArrayList to see if this x is within one unit of the given
    // cell's x
    for (int i = 0; i < this.grid.size(); i++) {
      ArrayList<Cell> row = this.grid.get(i);
      if (i + 1 == x || i - 1 == x || i == x) {
        int countY = 0;
        // loops through the Cells to see if this y is within one unit of the given
        // cell's y
        for (Cell n : row) {
          if ((countY + 1 == y || countY - 1 == y || (countY == y && i != x))) {
            c.neighbors.add(n);
          }
          countY++;
        }
      }
    }
  }

  public void onMouseClicked(Posn pos, String buttonName) {
    // left click to reveal tile, as long as the tile is not flagged
    if (buttonName.equals("LeftButton")
        && !this.grid.get(pos.y / IUtils.TILE_SIZE).get(pos.x / IUtils.TILE_SIZE).flagged) {
      this.grid.get(pos.y / IUtils.TILE_SIZE).get(pos.x / IUtils.TILE_SIZE).visible = true;
    }
    // right click to flag or unflag a tile
    if (buttonName.equals("RightButton")) {
      this.grid.get(pos.y / IUtils.TILE_SIZE).get(pos.x / IUtils.TILE_SIZE).flagged = !(this.grid
          .get(pos.y / IUtils.TILE_SIZE).get(pos.x / IUtils.TILE_SIZE).flagged);
    }

    // ends game when user wins
    // loops through each cell and counts the ones that are hidden
    int hidden = 0;
    for (int i = 0; i < this.height; i++) {
      for (int j = 0; j < this.width; j++) {

        Cell c = this.grid.get(i).get(j);

        if (!c.visible) {
          hidden++;
        }
      }
    }
    this.revealed = this.width * this.height - hidden;
    if (hidden == this.mineCount) {
      this.endOfWorld("You won!");
    }
    // ends game when user clicks on mine
    if (buttonName.equals("LeftButton")
        && this.grid.get(pos.y / IUtils.TILE_SIZE).get(pos.x / IUtils.TILE_SIZE).mine) {
      this.grid.get(pos.y / IUtils.TILE_SIZE).get(pos.x / IUtils.TILE_SIZE).visible = true;
      this.endOfWorld("You lost!");
    }
  }

  // displays a message on the screen if the user wins or loses
  public WorldScene lastScene(String msg) {
    WorldImage text = new TextImage(msg, IUtils.TILE_SIZE * 2, Color.blue);
    WorldScene scene = this.makeScene();
    scene.placeImageXY(text, this.gameWidth / 2, this.gameHeight / 2);

    // displays percentage of cells revealed
    TextImage score = new TextImage(
        "Revealed " + this.revealed * 100 / (this.width * this.height) + "% of the board",
        IUtils.TILE_SIZE, Color.black);
    scene.placeImageXY(score, this.gameWidth / 2, 4 * this.gameHeight / 5);
    return scene;
  }
}

// Class for Minesweeper Cell
class Cell {
  boolean mine;
  boolean flagged;
  boolean visible;
  ArrayList<Cell> neighbors;

  Cell() {
    this(false);
  }

  Cell(boolean mine) {
    this(mine, new ArrayList<Cell>());
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

class ExamplesMinesweeper {
  // Random seeds
  Random seed1 = new Random(1);
  Random seed2 = new Random(2);
  Random seed3 = new Random(3);
  Random seed4 = new Random(4);
  // MinesweeperWorld examples
  MinesweeperWorld smallBoard = new MinesweeperWorld(this.seed1, 4, 4, 6);
  ArrayList<ArrayList<Cell>> smallBoardGrid;
  MinesweeperWorld board1 = new MinesweeperWorld(this.seed2, 2, 2, 0);
  MinesweeperWorld board2 = new MinesweeperWorld(this.seed3, 2, 2, 0, true);
  // Cell examples
  Cell cellA1;
  Cell cellA2;
  Cell cellA3;
  Cell cellA4;
  Cell cellB1;
  Cell cellB2;
  Cell cellB3;
  Cell cellB4;
  Cell cellC1;
  Cell cellC2;
  Cell cellC3;
  Cell cellC4;
  Cell cellD1;
  Cell cellD2;
  Cell cellD3;
  Cell cellD4;
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;

  // WorldScene example -- 2x2 starting grid
  WorldScene ws = new WorldScene(board1.gameWidth, board1.gameHeight);
  WorldImage topCell = new OverlayImage(
      new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
      new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
  WorldImage topCellGrid = new AboveImage(new BesideImage(topCell, topCell),
      new BesideImage(topCell, topCell));

  // Initializes examples
  void reset() {
    // Initializes MinesweeperWorld examples
    this.seed1 = new Random(1);
    this.seed2 = new Random(2);
    this.seed3 = new Random(3);
    this.seed4 = new Random(4);
    this.smallBoard = new MinesweeperWorld(this.seed1, 4, 4, 6);
    this.smallBoardGrid = new ArrayList<ArrayList<Cell>>(Arrays.asList(
        new ArrayList<Cell>(Arrays.asList(this.cellA1, this.cellA2, this.cellA3, this.cellA4)),
        new ArrayList<Cell>(Arrays.asList(this.cellB1, this.cellB2, this.cellB3, this.cellB4)),
        new ArrayList<Cell>(Arrays.asList(this.cellC1, this.cellC2, this.cellC3, this.cellC4)),
        new ArrayList<Cell>(Arrays.asList(this.cellD1, this.cellD2, this.cellD3, this.cellD4))));
    // invalidBoard = new MinesweeperWorld(5, 5, 26);
    this.board1 = new MinesweeperWorld(this.seed2, 2, 2, 0);
    this.board2 = new MinesweeperWorld(this.seed3, 2, 2, 0, true);
    // Initializes cell examples
    this.cellA1 = new Cell(true,
        new ArrayList<Cell>(Arrays.asList(this.cellA2, this.cellB1, this.cellB2)));
    this.cellA2 = new Cell(false, new ArrayList<Cell>(
        Arrays.asList(this.cellA1, this.cellA3, this.cellB1, this.cellB2, this.cellB3)));
    this.cellA3 = new Cell(false, new ArrayList<Cell>(
        Arrays.asList(this.cellA2, this.cellA4, this.cellB2, this.cellB3, this.cellB4)));
    this.cellA4 = new Cell(false,
        new ArrayList<Cell>(Arrays.asList(this.cellA3, this.cellB3, this.cellB4)));
    this.cellB1 = new Cell(false, new ArrayList<Cell>(
        Arrays.asList(this.cellA1, this.cellA2, this.cellB2, this.cellC1, this.cellC2)));
    this.cellB2 = new Cell(true, new ArrayList<Cell>(Arrays.asList(this.cellA1, this.cellA2,
        this.cellA3, this.cellB1, this.cellB3, this.cellC1, this.cellC2, this.cellC3)));
    this.cellB3 = new Cell(true, new ArrayList<Cell>(Arrays.asList(this.cellA2, this.cellA3,
        this.cellA4, this.cellB2, this.cellB4, this.cellC2, this.cellC3, this.cellC4)));
    this.cellB4 = new Cell(false, new ArrayList<Cell>(
        Arrays.asList(this.cellA3, this.cellA4, this.cellB3, this.cellC3, this.cellC4)));
    this.cellC1 = new Cell(true, new ArrayList<Cell>(
        Arrays.asList(this.cellB1, this.cellB2, this.cellC2, this.cellD1, this.cellD2)));
    this.cellC2 = new Cell(false, new ArrayList<Cell>(Arrays.asList(this.cellB1, this.cellB2,
        this.cellB3, this.cellC1, this.cellC3, this.cellD1, this.cellD2, this.cellD3)));
    this.cellC3 = new Cell(false, new ArrayList<Cell>(Arrays.asList(this.cellB2, this.cellB3,
        this.cellB4, this.cellC2, this.cellC4, this.cellD2, this.cellD3, this.cellD4)));
    this.cellC4 = new Cell(false, new ArrayList<Cell>(
        Arrays.asList(this.cellB3, this.cellB4, this.cellC3, this.cellD3, this.cellD4)));
    this.cellD1 = new Cell(true,
        new ArrayList<Cell>(Arrays.asList(this.cellC1, this.cellC2, this.cellD2)));
    this.cellD2 = new Cell(false, new ArrayList<Cell>(
        Arrays.asList(this.cellC1, this.cellC2, this.cellC3, this.cellD1, this.cellD3)));
    this.cellD3 = new Cell(true, new ArrayList<Cell>(
        Arrays.asList(this.cellC2, this.cellC3, this.cellC4, this.cellD2, this.cellD4)));
    this.cellD4 = new Cell(false,
        new ArrayList<Cell>(Arrays.asList(this.cellC3, this.cellC4, this.cellD3)));
    this.cell1 = new Cell(false, false, true, new ArrayList<Cell>());
    this.cell2 = new Cell(false, false, true, new ArrayList<Cell>(Arrays.asList(this.cell1)));
    this.cell3 = new Cell(true, true, false, new ArrayList<Cell>(Arrays.asList(this.cell2)));
    this.cell4 = new Cell(false, false, true, new ArrayList<Cell>(Arrays.asList(this.cell3)));
    this.cell5 = new Cell(true, false, true, new ArrayList<Cell>(Arrays.asList(this.cell4)));

    // WorldScene example -- 2x2 starting grid
    ws = new WorldScene(board1.gameWidth, board1.gameHeight);
    topCell = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    topCellGrid = new AboveImage(new BesideImage(topCell, topCell),
        new BesideImage(topCell, topCell));
    ws.placeImageXY(topCellGrid, board1.gameWidth / 2, board1.gameWidth / 2);
  }

  // Checks if this cell is the same as the given cell
  boolean sameCell(Cell c1, Cell c2) {
    return c1.mine == c2.mine && c1.flagged == c2.flagged && c1.visible == c2.visible;
  }

  // Method for testing MinesweeperWorld
  // Tests initBoard, generateMines, createNeighbors, and updateNeighbors since
  // these methods are called
  void testMinesweeperWorld(Tester t) {
    this.reset();
    for (int i = 0; i < this.smallBoard.grid.size(); i++) {
      ArrayList<Cell> row = this.smallBoard.grid.get(i);
      ArrayList<Cell> expectedRow = this.smallBoardGrid.get(i);
      for (int j = 0; j < row.size(); j++) {
        Cell cell = row.get(j);
        Cell expectedCell = expectedRow.get(j);
        // Tests mines
        t.checkExpect(cell.mine, expectedCell.mine);
        for (int k = 0; k < cell.neighbors.size(); k++) {
          // tests each neighbor in each cell
          t.checkExpect(this.sameCell(cell.neighbors.get(k), expectedCell.neighbors.get(k)), true);
        }
      }
    }
  }

  // Runs the Minesweeper game
  void testBigBang(Tester t) {
    this.reset();
    MinesweeperWorld world = new MinesweeperWorld(15, 10, 20);
    // MinesweeperWorld world = new MinesweeperWorld(seed2, 15, 10, 5, true);
    int worldWidth = world.gameWidth;
    int worldHeight = world.gameHeight;
    world.bigBang(worldWidth, worldHeight);
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

  // tests the method makeScene in the class MinesweeperWorld
  void testMakeScene(Tester t) {
    // starting board
    this.reset();
    WorldScene scene = new WorldScene(board1.gameWidth, board1.gameHeight);
    WorldImage topBox = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    WorldImage squares = new AboveImage(new BesideImage(topBox, topBox),
        new BesideImage(topBox, topBox));
    scene.placeImageXY(squares, board1.gameWidth / 2, board1.gameWidth / 2);
    t.checkExpect(this.board1.makeScene(), scene);

    // starting board with flag
    this.reset();
    board1.grid.get(0).get(0).flagged = true;
    WorldScene scene1 = new WorldScene(board1.gameWidth, board1.gameHeight);
    WorldImage topBox1 = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    WorldImage squares1 = new AboveImage(new BesideImage(new OverlayImage(
        new EquilateralTriangleImage(IUtils.TILE_SIZE / 2, OutlineMode.SOLID, Color.cyan), topBox1),
        topBox1), new BesideImage(topBox1, topBox1));
    scene1.placeImageXY(squares1, board1.gameWidth / 2, board1.gameWidth / 2);
    t.checkExpect(this.board1.makeScene(), scene1);

    // intermediate board with no mines
    this.reset();
    WorldScene scene2 = new WorldScene(board2.gameWidth, board2.gameHeight);
    WorldImage box = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
            Color.LIGHT_GRAY));
    WorldImage squares2 = new AboveImage(new BesideImage(box, box), new BesideImage(box, box));
    scene2.placeImageXY(squares2, board2.gameWidth / 2, board2.gameWidth / 2);
    t.checkExpect(this.board2.makeScene(), scene2);

    // intermediate board with one mine
    this.reset();
    board2.grid.get(0).get(0).mine = true;
    WorldScene scene3 = new WorldScene(board2.gameWidth, board2.gameHeight);
    WorldImage textBox = new OverlayImage(new TextImage("1", IUtils.TILE_SIZE / 2, Color.black),
        new OverlayImage(
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE,
                Color.black),
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
                Color.LIGHT_GRAY)));
    WorldImage bombBox = new OverlayImage(
        new CircleImage(IUtils.TILE_SIZE / 3, OutlineMode.SOLID, Color.red),
        new OverlayImage(
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE,
                Color.black),
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
                Color.LIGHT_GRAY)));
    WorldImage squares3 = new AboveImage(new BesideImage(bombBox, textBox),
        new BesideImage(textBox, textBox));
    scene3.placeImageXY(squares3, board2.gameWidth / 2, board2.gameWidth / 2);
    t.checkExpect(this.board2.makeScene(), scene3);
  }

  // tests mouseEvent and win/loss conditions
  void testOnMouse(Tester t) {

    // starting board, no mines -- left click on top left square (tests flooding)
    this.reset();
    t.checkExpect(this.board1.makeScene(), ws);
    WorldScene scene = new WorldScene(board1.gameWidth, board1.gameHeight);
    this.board1.onMouseClicked(new Posn(1, 1), "LeftButton");
    WorldImage box = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
            Color.LIGHT_GRAY));
    WorldImage squares = new AboveImage(new BesideImage(box, box), new BesideImage(box, box));
    scene.placeImageXY(squares, board1.gameWidth / 2, board1.gameWidth / 2);
    t.checkExpect(this.board1.makeScene(), scene);

    // starting board, one mine in bottom right -- left click on top left square (no
    // flooding)
    this.reset();
    t.checkExpect(this.board1.makeScene(), ws);
    board1.grid.get(1).get(1).mine = true;
    WorldScene scene1 = new WorldScene(board1.gameWidth, board1.gameHeight);
    this.board1.onMouseClicked(new Posn(1, 1), "LeftButton");
    WorldImage topBox = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    WorldImage textBox = new OverlayImage(new TextImage("1", IUtils.TILE_SIZE / 2, Color.black),
        new OverlayImage(
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE,
                Color.black),
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
                Color.LIGHT_GRAY)));
    WorldImage squares1 = new AboveImage(new BesideImage(textBox, topBox),
        new BesideImage(topBox, topBox));
    scene1.placeImageXY(squares1, board1.gameWidth / 2, board1.gameWidth / 2);
    t.checkExpect(this.board1.makeScene(), scene1);

    // starting board, one mine in top left -- left click on top left square (tests
    // loss conditions)
    this.reset();
    t.checkExpect(this.board1.makeScene(), ws);
    board1.grid.get(0).get(0).mine = true;
    WorldScene scene2 = new WorldScene(board1.gameWidth, board1.gameHeight);
    this.board1.onMouseClicked(new Posn(1, 1), "LeftButton");
    WorldImage topBox1 = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    WorldImage bombBox = new OverlayImage(
        new CircleImage(IUtils.TILE_SIZE / 3, OutlineMode.SOLID, Color.red),
        new OverlayImage(
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE,
                Color.black),
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
                Color.LIGHT_GRAY)));
    WorldImage squares2 = new AboveImage(new BesideImage(bombBox, topBox1),
        new BesideImage(topBox1, topBox1));
    scene2.placeImageXY(squares2, board1.gameWidth / 2, board1.gameWidth / 2);
    scene2.placeImageXY(new TextImage("You lost!", IUtils.TILE_SIZE * 2, Color.blue),
        board1.gameWidth / 2, board1.gameHeight / 2);
    TextImage score1 = new TextImage(
        "Revealed " + board1.revealed * 100 / (board1.width * board1.height) + "% of the board",
        IUtils.TILE_SIZE, Color.black);
    scene2.placeImageXY(score1, board1.gameWidth / 2, 4 * board1.gameHeight / 5);
    t.checkExpect(this.board1.lastScene("You lost!"), scene2);

    // tests win conditions
    this.reset();
    board1.grid.get(0).get(0).mine = true;
    board1.grid.get(0).get(1).visible = true;
    board1.grid.get(1).get(0).visible = true;
    board1.grid.get(1).get(1).visible = true;
    WorldScene scene3 = new WorldScene(board1.gameWidth, board1.gameHeight);
    WorldImage topBox2 = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    WorldImage textBox1 = new OverlayImage(new TextImage("1", IUtils.TILE_SIZE / 2, Color.black),
        new OverlayImage(
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE,
                Color.black),
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID,
                Color.LIGHT_GRAY)));
    WorldImage squares3 = new AboveImage(new BesideImage(topBox2, textBox1),
        new BesideImage(textBox1, textBox1));
    scene3.placeImageXY(squares3, board1.gameWidth / 2, board1.gameWidth / 2);
    scene3.placeImageXY(new TextImage("You won!", IUtils.TILE_SIZE * 2, Color.blue),
        board1.gameWidth / 2, board1.gameHeight / 2);
    TextImage score2 = new TextImage(
        "Revealed " + board1.revealed * 100 / (board1.width * board1.height) + "% of the board",
        IUtils.TILE_SIZE, Color.black);
    scene3.placeImageXY(score2, board1.gameWidth / 2, 4 * board1.gameHeight / 5);
    t.checkExpect(this.board1.lastScene("You won!"), scene3);

    // tests flagging top left cell by right click
    this.reset();
    t.checkExpect(this.board1.makeScene(), ws);
    WorldScene scene4 = new WorldScene(board1.gameWidth, board1.gameHeight);
    this.board1.onMouseClicked(new Posn(1, 1), "RightButton");
    WorldImage topBox3 = new OverlayImage(
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE, Color.black),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY));
    WorldImage flagBox = new OverlayImage(
        new EquilateralTriangleImage(IUtils.TILE_SIZE / 2, OutlineMode.SOLID, Color.cyan),
        new OverlayImage(
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.OUTLINE,
                Color.black),
            new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.GRAY)));
    WorldImage squares4 = new AboveImage(new BesideImage(flagBox, topBox3),
        new BesideImage(topBox3, topBox3));
    scene4.placeImageXY(squares4, board1.gameWidth / 2, board1.gameWidth / 2);
    t.checkExpect(this.board1.makeScene(), scene4);
    // tests if user can reveal the newly flagged cell
    this.board1.onMouseClicked(new Posn(1, 1), "LeftButton");
    t.checkExpect(this.board1.makeScene(), scene4);
    // tests unflagging it right after
    this.board1.onMouseClicked(new Posn(1, 1), "RightButton");
    t.checkExpect(this.board1.makeScene(), ws);

  }
}