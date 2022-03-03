# Nonogrammatica
## About the puzzle
The Nonogram/Hanjie/Paint by Numbers/Picross/Griddlers/Pic-a-Pix logic puzzle originates from Japan. The objective is to color in squares in a grid to reveal a bitmap image.

You are given the length/height of each sequence of filled squares occurring within each respective row/column of the grid, and must deduce which cells necessarily are filled or empty. As you fill in and cross out squares in one row or column, more information is obtained about the perpendicular column or row.

"Guess-and-check" or "if-then" logic should not be necessary to solve a well-designed nonogram. The solver in Nonogrammatica will determine if the puzzle you draw or provide is solvable, so long as this is not necessary to solve.

## About the project
To build, you will need JavaFX: https://openjfx.io/

The app allows you to draw your own Nonogram and check whether it is solvable (in-so-far-as "if-then" logic is not needed). If it is not solvable, it will show you how far one can get in a solution attempt, and if it is solvable will allow you to export the unsolved or solved puzzle to .png for use as a sharable puzzle and key to that puzzle. It also allows you to input the row and column numbers of a Nonogram from an outside source directly and run the same solving routine on that puzzle.
