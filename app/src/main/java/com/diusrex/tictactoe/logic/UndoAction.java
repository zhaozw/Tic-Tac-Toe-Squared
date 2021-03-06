/*
 * Copyright 2015 Morgan Redshaw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.diusrex.tictactoe.logic;

import java.util.Stack;

public class UndoAction {
    public static void undoLastMove(BoardStatus board) {
        Stack<Move> allMoves = board.getAllMoves();
        // Cannot do anything in this case
        if (allMoves.size() == 0)
            return;

        // Take out the top move
        Move topMove = allMoves.pop();

        restoreBoxToUnowned(board, topMove.getPosition());

        if (moveLostOwnership(board, topMove))
            board.setSectionOwner(topMove.getSectionIn(), null, Player.Unowned);

        restoreSectionToPlayIn(board, allMoves, topMove.getPosition());
    }

    private static void restoreBoxToUnowned(BoardStatus board, BoxPosition pos) {
        board.setBoxOwner(pos, Player.Unowned);
    }

    private static boolean moveLostOwnership(BoardStatus board, Move topMove) {
        // In this case, it is impossible
        if (topMove.getPlayer() != board.getSectionOwner(topMove.getSectionIn()))
            return false;

        SectionPosition sectionIn = topMove.getSectionIn();
        Player completedWinner = GridChecker.searchForPattern(board.getBoxGrid(), sectionIn);

        // If there is no found match, then the grid was lost
        return completedWinner == Player.Unowned;
    }

    private static void restoreSectionToPlayIn(BoardStatus board, Stack<Move> allMoves, BoxPosition topMove) {
        SectionPosition sectionToRestoreTo;

        // If > 0, can get where to play from previous move
        if (allMoves.size() > 0) {
            Move previousMove = allMoves.peek();

            sectionToRestoreTo = TicTacToeEngine.getSectionToPlayInNext(previousMove);
        } else {
            sectionToRestoreTo = topMove.getSectionIn();
        }

        board.setSectionToPlayIn(sectionToRestoreTo);
    }
}
