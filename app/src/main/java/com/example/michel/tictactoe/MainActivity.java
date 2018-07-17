package com.example.michel.tictactoe;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Game implementation
    boolean isXTurn = true;
    boolean isOTurn = false;
    int boardWidth = 3;
    int boardHeight = 3;
    char[][] board = new char[boardHeight][boardWidth];
    String winner;
    boolean isOnePlayerGame = false;
    String onePlayerGameDifficulty = "easy";
    int[] lastMoveX = new int[2];
    int[] lastMoveO = new int[2];
    int xWins = 0;
    int oWins = 0;

    //Layout implementation
    Button[][] boardOfButtons = new Button[boardHeight][boardWidth];
    Button button00;
    Button button10;
    Button button20;
    Button button01;
    Button button11;
    Button button21;
    Button button02;
    Button button12;
    Button button22;
    TextView player1NameTextView;
    TextView player1ScoreTextView;
    TextView player2NameTextView;
    TextView player2ScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeButtons();
        initializeBoardOfButtons();

        player1NameTextView = (TextView) findViewById(R.id.tv_player1_name);
        player1ScoreTextView = (TextView) findViewById(R.id.tv_player1_score);
        player2NameTextView = (TextView) findViewById(R.id.tv_player2_name);
        player2ScoreTextView = (TextView) findViewById(R.id.tv_player2_score);

        try {
            FileInputStream fileInputStream = openFileInput("board");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            board = (char[][]) objectInputStream.readObject();
            isXTurn = objectInputStream.readBoolean();
            isOTurn = objectInputStream.readBoolean();
            winner = (String) objectInputStream.readObject();
            isOnePlayerGame = objectInputStream.readBoolean();
            onePlayerGameDifficulty = (String) objectInputStream.readObject();
            xWins = objectInputStream.readInt();
            oWins = objectInputStream.readInt();
        } catch (IOException e) {
            File file = new File(MainActivity.this.getFilesDir(), "board");
            Log.e("IOException", "Board database file not found.");
        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFoundException", "Board not found in database.");
        }

        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j] == 'X') {
                    boardOfButtons[i][j].setText("X");
                    boardOfButtons[i][j].setTextColor(Color.parseColor("#F44336"));
                } else if (board[i][j] == 'O') {
                    boardOfButtons[i][j].setText("O");
                    boardOfButtons[i][j].setTextColor(Color.parseColor("#2196F3"));
                }
            }
        }

        player1ScoreTextView.setText(String.valueOf(xWins));
        player2ScoreTextView.setText(String.valueOf(oWins));

        if (isOnePlayerGame) {
            player1NameTextView.setText(R.string.player);
            player2NameTextView.setText(R.string.computer);
        }
    }

    private void initializeButtons() {
        button00 = (Button) findViewById(R.id._00_button);
        button10 = (Button) findViewById(R.id._10_button);
        button20 = (Button) findViewById(R.id._20_button);
        button01 = (Button) findViewById(R.id._01_button);
        button11 = (Button) findViewById(R.id._11_button);
        button21 = (Button) findViewById(R.id._21_button);
        button02 = (Button) findViewById(R.id._02_button);
        button12 = (Button) findViewById(R.id._12_button);
        button22 = (Button) findViewById(R.id._22_button);
    }

    private void initializeBoardOfButtons() {
        boardOfButtons[0][0] = button00;
        boardOfButtons[0][1] = button10;
        boardOfButtons[0][2] = button20;
        boardOfButtons[1][0] = button01;
        boardOfButtons[1][1] = button11;
        boardOfButtons[1][2] = button21;
        boardOfButtons[2][0] = button02;
        boardOfButtons[2][1] = button12;
        boardOfButtons[2][2] = button22;
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            FileOutputStream fileOutputStream = openFileOutput("board", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(board);
            objectOutputStream.writeBoolean(isXTurn);
            objectOutputStream.writeBoolean(isOTurn);
            objectOutputStream.writeObject(winner);
            objectOutputStream.writeBoolean(isOnePlayerGame);
            objectOutputStream.writeObject(onePlayerGameDifficulty);
            objectOutputStream.writeInt(xWins);
            objectOutputStream.writeInt(oWins);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the button in the top left corner is pressed, check whose turn it is. Then modify the board accordingly.
     */
    public void onClickButton(View view) {

        boolean gameWasReset = false;

        if (winner != null) {
            resetGame(false);
            gameWasReset = true;
        }

        if (!gameWasReset) {
            int yPosition = 0;
            int xPosition = 0;
            for (int i = 0; i < boardHeight; i++) {
                for (int j = 0; j < boardWidth; j++) {
                    if (boardOfButtons[i][j] == view) {
                        yPosition = i;
                        xPosition = j;
                    }
                }
            }

            if (((Button) view).getText().toString().isEmpty() && winner == null) {
                if (isXTurn) {
                    board[yPosition][xPosition] = 'X';
                    lastMoveX[0] = yPosition;
                    lastMoveX[1] = xPosition;
                    setButtonX((Button) view);
                } else if (isOTurn) {
                    board[yPosition][xPosition] = 'O';
                    lastMoveO[0] = yPosition;
                    lastMoveO[1] = xPosition;
                    setButtonO((Button) view);
                }
            }

            winner = checkWin();

            if (winner != null) {
                switch (winner) {
                    case "X":
                        Toast toastX = Toast.makeText(MainActivity.this, "X Wins\nPress Any Square To Play Again", Toast.LENGTH_SHORT);
                        TextView messageX = (TextView) toastX.getView().findViewById(android.R.id.message);
                        messageX.setGravity(Gravity.CENTER);
                        toastX.show();
                        xWins++;
                        player1ScoreTextView.setText(String.valueOf(xWins));
                        break;
                    case "O":
                        Toast toastO = Toast.makeText(MainActivity.this, "O Wins\nPress Any Square To Play Again", Toast.LENGTH_SHORT);
                        TextView messageO = (TextView) toastO.getView().findViewById(android.R.id.message);
                        messageO.setGravity(Gravity.CENTER);
                        toastO.show();
                        oWins++;
                        player2ScoreTextView.setText(String.valueOf(oWins));
                        break;
                    case "Draw":
                        Toast toastDraw = Toast.makeText(MainActivity.this, "Draw\nPress Any Square To Play Again", Toast.LENGTH_SHORT);
                        TextView messageDraw = (TextView) toastDraw.getView().findViewById(android.R.id.message);
                        messageDraw.setGravity(Gravity.CENTER);
                        toastDraw.show();
                        break;
                    default:
                        break;
                }
            }

            if (isOnePlayerGame && isOTurn) {
                makeMove();
            }
        }
    }

    private String checkWin() {
        //Horizontal check
        for (int i = 0; i < boardHeight; i++) {
            int xCountHorizontal = 0;
            int oCountHorizontal = 0;
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j] == 'X') {
                    xCountHorizontal++;
                } else if (board[i][j] == 'O') {
                    oCountHorizontal++;
                }
            }
            if (xCountHorizontal == 3) {
                return "X";
            } else if (oCountHorizontal == 3) {
                return "O";
            }
        }

        //Vertical check
        for (int i = 0; i < boardWidth; i++) {
            int xCountVertical = 0;
            int oCountVertical = 0;
            for (int j = 0; j < boardHeight; j++) {
                if (board[j][i] == 'X') {
                    xCountVertical++;
                } else if (board[j][i] == 'O') {
                    oCountVertical++;
                }
            }
            if (xCountVertical == 3) {
                return "X";
            } else if (oCountVertical == 3) {
                return "O";
            }
        }

        //Diagonal left to right check
        int xCountDiagonal = 0;
        int oCountDiagonal = 0;
        for (int i = 0; i < boardHeight; i++) {
            if (board[i][i] == 'X') {
                xCountDiagonal++;
            } else if (board[i][i] == 'O') {
                oCountDiagonal++;
            }
        }
        if (xCountDiagonal == 3) {
            return "X";
        } else if (oCountDiagonal == 3) {
            return "O";
        }

        //Diagonal right to left check
        if (board[0][2] == 'X' && board[1][1] == 'X' && board[2][0] == 'X') {
            return "X";
        } else if (board[0][2] == 'O' && board[1][1] == 'O' && board[2][0] == 'O') {
            return "O";
        }

        //Finally check for draw
        if (winner == null) {
            int filledTiles = 0;
            for (int i = 0; i < boardHeight; i++) {
                for (int j = 0; j < boardWidth; j++) {
                    if (board[i][j] != 0) {
                        filledTiles++;
                    }
                }
            }
            if (filledTiles == 9) {
                return "Draw";
            }
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset_menu) {
            resetGame(true);
        } else if (item.getItemId() == R.id.one_player_menu) {
            isOnePlayerGame = true;
            player1NameTextView.setText(R.string.player);
            player2NameTextView.setText(R.string.computer);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose difficulty");
            builder.setItems(R.array.difficulties, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        onePlayerGameDifficulty = "easy";
                    } else if (which == 1) {
                        onePlayerGameDifficulty = "medium";
                    } else if (which == 2) {
                        onePlayerGameDifficulty = "hard";
                    } else if (which == 3) {
                        onePlayerGameDifficulty = "impossible";
                    }
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            resetGame(true);
        } else if (item.getItemId() == R.id.two_player_menu) {
            isOnePlayerGame = false;
            player1NameTextView.setText(R.string.player_one);
            player2NameTextView.setText(R.string.player_two);
            resetGame(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetGame(boolean resetScore) {
        board = new char[boardHeight][boardWidth];
        winner = null;
        isXTurn = true;
        isOTurn = false;

        if (resetScore) {
            xWins = 0;
            oWins = 0;
            player1ScoreTextView.setText("0");
            player2ScoreTextView.setText("0");
        }

        button00.setText("");
        button10.setText("");
        button20.setText("");
        button01.setText("");
        button11.setText("");
        button21.setText("");
        button02.setText("");
        button12.setText("");
        button22.setText("");
    }

    private void makeMove() {

        if (onePlayerGameDifficulty.equals("easy")) {

            Random random = new Random();
            boolean donePicking = false;

            while (!donePicking && winner == null) {
                int randomX = random.nextInt(3);
                int randomY = random.nextInt(3);
                if (board[randomY][randomX] == 0) {
                    boardOfButtons[randomY][randomX].performClick();
                    donePicking = true;
                }
            }

        } else if (onePlayerGameDifficulty.equals("medium")) {

            Random random = new Random();
            boolean madeAWinningMove = false;
            for (int i = 0; i < boardHeight; i++) {
                for (int j = 0; j < boardWidth; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = 'O';
                        String winner = checkWin();
                        if (winner != null && winner.equals("O")) {
                            boardOfButtons[i][j].performClick();
                            madeAWinningMove = true;
                            break;
                        } else {
                            board[i][j] = 0;
                        }
                    }
                }
                if (madeAWinningMove) {
                    break;
                }
            }

            if (!madeAWinningMove) {
                boolean donePicking = false;

                while (!donePicking && winner == null) {
                    int randomX = random.nextInt(3);
                    int randomY = random.nextInt(3);
                    if (board[randomY][randomX] == 0) {
                        boardOfButtons[randomY][randomX].performClick();
                        donePicking = true;
                    }
                }
            }

        } else if (onePlayerGameDifficulty.equals("hard")) {

            Random random = new Random();
            boolean madeAWinningMove = false;

            if (winner == null) {
                for (int i = 0; i < boardHeight; i++) {
                    for (int j = 0; j < boardWidth; j++) {
                        if (board[i][j] == 0) {
                            board[i][j] = 'O';
                            String winner = checkWin();
                            if (winner != null && winner.equals("O")) {
                                boardOfButtons[i][j].performClick();
                                madeAWinningMove = true;
                                break;
                            } else {
                                board[i][j] = 0;
                            }
                        }
                    }
                    if (madeAWinningMove) {
                        break;
                    }
                }

                if (!madeAWinningMove) {
                    for (int i = 0; i < boardHeight; i++) {
                        for (int j = 0; j < boardWidth; j++) {
                            if (board[i][j] == 0) {
                                board[i][j] = 'X';
                                String winner = checkWin();
                                if (winner != null && winner.equals("X")) {
                                    boardOfButtons[i][j].performClick();
                                    madeAWinningMove = true;
                                    break;
                                } else {
                                    board[i][j] = 0;
                                }
                            }
                        }
                        if (madeAWinningMove) {
                            break;
                        }
                    }
                }

                if (!madeAWinningMove) {
                    boolean donePicking = false;

                    while (!donePicking && winner == null) {
                        int randomX = random.nextInt(3);
                        int randomY = random.nextInt(3);
                        if (board[randomY][randomX] == 0) {
                            boardOfButtons[randomY][randomX].performClick();
                            donePicking = true;
                        }
                    }
                }
            }
        } else if (onePlayerGameDifficulty.equals("impossible")) {

            Random random = new Random();
            boolean madeAWinningMove = false;

            for (int i = 0; i < boardHeight; i++) {
                for (int j = 0; j < boardWidth; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = 'O';
                        String winner = checkWin();
                        if (winner != null && winner.equals("O")) {
                            boardOfButtons[i][j].performClick();
                            madeAWinningMove = true;
                            break;
                        } else {
                            board[i][j] = 0;
                        }
                    }
                }
                if (madeAWinningMove) {
                    break;
                }
            }

            if (!madeAWinningMove) {
                for (int i = 0; i < boardHeight; i++) {
                    for (int j = 0; j < boardWidth; j++) {
                        if (board[i][j] == 0) {
                            board[i][j] = 'X';
                            String winner = checkWin();
                            if (winner != null && winner.equals("X")) {
                                boardOfButtons[i][j].performClick();
                                madeAWinningMove = true;
                                break;
                            } else {
                                board[i][j] = 0;
                            }
                        }
                    }
                    if (madeAWinningMove) {
                        break;
                    }
                }
            }

            if (!madeAWinningMove && lastMoveX[0] == 1 && lastMoveX[1] == 1) {
                if (board[0][0] == 0) {
                    boardOfButtons[0][0].performClick();
                    madeAWinningMove = true;
                } else if (board[0][2] == 0) {
                    boardOfButtons[0][2].performClick();
                    madeAWinningMove = true;
                } else if (board[2][0] == 0) {
                    boardOfButtons[2][0].performClick();
                    madeAWinningMove = true;
                } else if (board[2][2] == 0) {
                    boardOfButtons[2][2].performClick();
                    madeAWinningMove = true;
                }
            }

            if (!madeAWinningMove) {
                if (!(lastMoveX[0] == 1 && lastMoveX[1] == 1) && board[1][1] == 0) {
                    boardOfButtons[1][1].performClick();
                    madeAWinningMove = true;
                }
            }

            if (!madeAWinningMove) {
                if (board[1][1] == 'X' && lastMoveX[0] == 2 && lastMoveX[1] == 2) {
                    if (board[0][2] == 0) {
                        boardOfButtons[0][2].performClick();
                        madeAWinningMove = true;
                    } else if (board[2][0] == 0) {
                        boardOfButtons[2][0].performClick();
                        madeAWinningMove = true;
                    }
                }
            }

            if (!madeAWinningMove) {
                if (lastMoveO[0] == 1 && lastMoveO[1] == 1) {
                    if (board[0][1] == 0) {
                        boardOfButtons[0][1].performClick();
                        madeAWinningMove = true;
                    } else if (board[1][0] == 0) {
                        boardOfButtons[1][0].performClick();
                        madeAWinningMove = true;
                    } else if (board[1][2] == 0) {
                        boardOfButtons[1][2].performClick();
                        madeAWinningMove = true;
                    } else if (board[2][1] == 0) {
                        boardOfButtons[2][1].performClick();
                        madeAWinningMove = true;
                    }
                }
            }

            if (!madeAWinningMove) {
                if (lastMoveX[0] == 0 && lastMoveX[1] == 0 && board[2][2] == 0) {
                    boardOfButtons[2][2].performClick();
                    madeAWinningMove = true;
                } else if (lastMoveX[0] == 0 && lastMoveX[1] == 2 && board[2][0] == 0) {
                    boardOfButtons[2][0].performClick();
                    madeAWinningMove = true;
                } else if (lastMoveX[0] == 2 && lastMoveX[1] == 0 && board[0][2] == 0) {
                    boardOfButtons[0][2].performClick();
                    madeAWinningMove = true;
                } else if (lastMoveX[0] == 2 && lastMoveX[1] == 2 && board[0][0] == 0) {
                    boardOfButtons[0][0].performClick();
                    madeAWinningMove = true;
                }
            }

            if (!madeAWinningMove) {
                boolean donePicking = false;

                while (!donePicking && winner == null) {
                    int randomX = random.nextInt(3);
                    int randomY = random.nextInt(3);
                    if (board[randomY][randomX] == 0) {
                        boardOfButtons[randomY][randomX].performClick();
                        donePicking = true;
                    }
                }
            }

        }
    }

    private void setButtonO(Button button) {
        button.setText("O");
        button.setTextColor(Color.parseColor("#2196F3"));
        isXTurn = true;
        isOTurn = false;
    }

    private void setButtonX(Button button) {
        button.setText("X");
        button.setTextColor(Color.parseColor("#F44336"));
        isXTurn = false;
        isOTurn = true;
    }
}


