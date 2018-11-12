package game;

import java.util.*;

public class Model {
    protected int score;
    protected int maxTile;
    private static final int FIELD_WIDTH = 4;
    private    Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    private Stack<Tile[][]> previousStates = new Stack<Tile[][]>();
    private Stack<Integer> previousScores = new Stack<Integer>();
    private boolean isSaveNeeded = true;


    private void saveState(Tile[][] tiles){
        Tile[][] save = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0 ; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
                save[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(save);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if(!previousScores.isEmpty() & !previousStates.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }//2. Публичный метод rollback будет устанавливать текущее игровое состояние равным последнему находящемуся в стеках с помощью метода pop.

    public Model() {
        resetGameTiles();

    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    void resetGameTiles(){
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private void addTile(){
        List<Tile> list = getEmptyTiles();
        if(list.size() > 0) {
            Tile tile = list.get((int) (list.size() * Math.random()));
            tile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> list = new ArrayList<>();
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
                if(gameTiles[i][j].isEmpty()){
                    list.add(gameTiles[i][j]);
                }
            }
        }
        return list;
    }

    private boolean compressTiles(Tile[] tiles){
        boolean b = false;
        for(int i = tiles.length-1 ; i > 0 ; i--){
            for(int j = 0 ; j < i ; j++){
                if( tiles[j].value == 0 && tiles[j+1].value != 0 ) {
                    b = true;
                    int tmp = tiles[j].value;
                    tiles[j].value = tiles[j+1].value;
                    tiles[j+1].value = tmp;
                }
            }
        }
        return b;
    }

    private boolean  mergeTiles(Tile[] tiles){
        boolean b =  false;
        for(int i = 0;i< tiles.length-1; i++){
            if(tiles[i].value == tiles[i+1].value && tiles[i].value > 0){
                b = true;
                tiles[i].value *= 2;
                tiles[i+1].value = 10;
                for(int j = i+2; j < tiles.length; j++){
                    tiles[j-1].value = tiles [j].value;
                }
                tiles[tiles.length - 1].value = 0;
                score += tiles[i].value;
                if (maxTile < tiles[i].value)
                    maxTile = tiles[i].value;
            }
        }
        return b;
    }

    public void left(){
        saveState(gameTiles);
        boolean b = false;
        for(int i = 0; i < FIELD_WIDTH;i++){
            if(compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])){
                b = true;
            }
        }
        if(b)
            addTile();
    }
    public void right(){
        saveState(gameTiles);
        boolean b = false;
        for(int i = 0; i < FIELD_WIDTH;i++){
            Tile[] tiles = new Tile[FIELD_WIDTH];
            for(int j = FIELD_WIDTH- 1, k = 0; j > -1; j-- ,k++){
                tiles[k] = gameTiles[i][j];
            }
            if(compressTiles(tiles) | mergeTiles(tiles)){
                b = true;
            }
        }
        if(b)
            addTile();
    }

    public void up(){
        saveState(gameTiles);
        boolean b = false;
        for(int j = 0; j < FIELD_WIDTH; j++){
            Tile[] tiles = new Tile[FIELD_WIDTH];
            for(int i = 0 ; i < FIELD_WIDTH; i++){
                tiles[i] = gameTiles[i][j];
            }
            if(compressTiles(tiles) | mergeTiles(tiles)){
                b = true;
            }
        }
        if(b)
            addTile();
    }

    public void down(){
        saveState(gameTiles);
        for(int j = 0; j < FIELD_WIDTH; j++){
            Tile[] tiles = new Tile[FIELD_WIDTH];
            for(int i = FIELD_WIDTH - 1 , k = 0; i > -1; i--,k++){
                tiles[k] = gameTiles[i][j];
            }
            if(compressTiles(tiles) & mergeTiles(tiles)){
                addTile();
            }
        }
    }

    public boolean canMove(){
        boolean b = false;
        for(int i = 0; i < FIELD_WIDTH; i ++){
            for(int j = 0; j < FIELD_WIDTH; j ++){
                if(gameTiles[i][j].isEmpty())
                    b = true;
            }
            for(int j = 0; j < FIELD_WIDTH-1; j ++){
                if(gameTiles[i][j].value == gameTiles[i][j+1].value)
                    b = true;
            }
        }
        for(int j = 0; j < FIELD_WIDTH; j ++){
            for(int i = 0; i < FIELD_WIDTH-1; i ++){
                if(gameTiles[i][j].value == gameTiles[i+1][j].value)
                    b = true;
            }
        }
        return b;
    }

    public void randomMove(){
        int n = (int) (Math.random()*100)/4;
        if(n == 0)
            left();
        if (n==1)
            right();
        if (n==2)
            up();
        if(n==3)
            down();
    }

    public boolean hasBoardChanged(){
        int one = 0;
        int two = 0;
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
              one += gameTiles[i][j].value;
              two += previousStates.peek()[i][j].value;
            }
        }
        if(one == two)
            return false;
        else
            return true;
    }
    public MoveEfficiency getMoveEfficiency(Move move){
        move.move();
        if( hasBoardChanged()){
            rollback();
            return new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return new MoveEfficiency(-1,0,move);
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4,Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(this::left));
        priorityQueue.add(getMoveEfficiency(this::right));
        priorityQueue.add(getMoveEfficiency(this::up));
        priorityQueue.add(getMoveEfficiency(this::down));
        priorityQueue.peek().getMove().move();
    }
}

//Давай реализуем метод autoMove в классе Model, который будет выбирать лучший из возможных ходов и выполнять его.
//
//Сделаем так:
//1) Создадим локальную PriorityQueue с параметром Collections.reverseOrder() (для того, чтобы вверху очереди всегда был максимальный элемент) и размером равным четырем.
//2) Заполним PriorityQueue четырьмя объектами типа MoveEfficiency (по одному на каждый вариант хода).
//3) Возьмем верхний элемент и выполним ход связанный с ним.
//
//После реализации метода autoMove добавим его вызов в метод keyPressed класса Controller по нажатию на клавишу A (код - KeyEvent.VK_A).