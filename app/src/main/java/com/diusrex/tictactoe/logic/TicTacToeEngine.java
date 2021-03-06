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

public class TicTacToeEngine {


    public static boolean isValidMove(BoardStatus board, Move move) {
        // Should be able to handle this properly
        if (move == null)
            return false;

        return isInsideBounds(board, move) && isInOrder(board, move) && isInCorrectSection(board, move)
                && isNotOwned(board, move) && move.getPlayer() != Player.Unowned;
    }

    public static void applyMoveIfValid(BoardStatus board, Move move) {
        if (isValidMove(board, move)) {
            applyMove(board, move);
        }
    }

    private static void applyMove(BoardStatus board, Move move) {
        board.applyMove(move);
        board.setSectionToPlayIn(getSectionToPlayInNext(move));
        updateSectionOwner(board, move);
    }

    private static boolean isInsideBounds(BoardStatus board, Move move) {
        return board.isInsideBounds(move.getPosition());
    }

    private static boolean isInOrder(BoardStatus board, Move move) {
        int p1Count = board.getPlayerCount(Player.Player_1);
        int p2Count = board.getPlayerCount(Player.Player_2);

        if (move.getPlayer() == Player.Player_1)
            ++p1Count;

        else
            ++p2Count;

        return p1Count == p2Count || p1Count == p2Count + 1;
    }

    private static boolean isNotOwned(BoardStatus board, Move move) {
        return board.getBoxOwner(move.getPosition()) == Player.Unowned;
    }

    private static boolean isInCorrectSection(BoardStatus board, Move move) {
        SectionPosition requiredSection = board.getSectionToPlayIn();
        SectionPosition actualSection = move.getSectionIn();

        // Checks if it is in the correct section
        return requiredSection.equals(actualSection) || sectionIsFull(board, requiredSection);
    }

    private static boolean sectionIsFull(BoardStatus board, SectionPosition requiredSection) {
        BoxPosition offset = requiredSection.getTopLeftPosition();

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                BoxPosition positionInSection = BoxPosition.make(x, y);
                if (board.getBoxOwner(positionInSection.increaseBy(offset)) == Player.Unowned)
                    return false;
            }
        }

        return true;
    }

    private static void updateSectionOwner(BoardStatus board, Move move) {
        SectionPosition changedSection = move.getSectionIn();
        // Cannot take a section from other player
        if (board.getSectionOwner(changedSection) != Player.Unowned)
            return;

        Player detectedSectionOwner = GridChecker.searchForPattern(board.getBoxGrid(), changedSection);
        if (detectedSectionOwner != Player.Unowned) {
            Line winLine = GridChecker.searchForLineOrGetNull(board.getBoxGrid(), changedSection);
            board.setSectionOwner(changedSection, winLine, detectedSectionOwner);
        }
    }

    public static SectionPosition getSectionToPlayInNext(Move move) {
        return getSectionToPlayInNext(move.getPosition());
    }

    public static SectionPosition getSectionToPlayInNext(BoxPosition pos) {
        SectionPosition sectionIn = pos.getSectionIn();
        pos = pos.decreaseBy(sectionIn.getTopLeftPosition());

        return SectionPosition.make(pos.getX(), pos.getY());
    }

    public static Player getWinner(BoardStatus board) {
        return GridChecker.searchForPattern(board.getOwnerGrid());
    }

    static final int SIZE_OF_SAVED_MOVE = 3;

    public static String getSaveString(BoardStatus board) {
        Stack<Move> allMoves = board.getAllMoves();
        StringBuffer buffer = new StringBuffer();
        for (Move move : allMoves) {
            buffer.append(moveToString(move));
        }

        return buffer.toString();
    }

    private static String moveToString(Move m) {
        return String.format("%d%d%s", m.getPosition().getX(), m.getPosition().getY(), m.getPlayer().toString());
    }

    // Will allow any move to be played
    public static BoardStatus loadBoardFromString(String savedBoardStatus) {
        BoardStatus board = new BoardStatus();
        for (int i = 0; i + SIZE_OF_SAVED_MOVE - 2 < savedBoardStatus.length(); i += SIZE_OF_SAVED_MOVE) {
            Move move = stringToMove(savedBoardStatus.substring(i, i + SIZE_OF_SAVED_MOVE));
            applyMove(board, move);
        }

        return board;
    }

    public static Move stringToMove(String substring) {
        int totalValue = Integer.parseInt(substring);
        Player mainPlayer = Player.values()[totalValue % 10];
        int x = totalValue / 100;
        int y = (totalValue / 10) % 10;

        return new Move(BoxPosition.make(x, y), mainPlayer);
    }

    public static String sectionPositionToString(SectionPosition sectionPosition) {
        return String.format("%d%d", sectionPosition.getX(), sectionPosition.getY());
    }

    public static SectionPosition stringToSectionPosition(String substring) {
        int totalValue = Integer.parseInt(substring);
        int x = totalValue / 10;
        int y = totalValue % 10;

        return SectionPosition.make(x, y);
    }

    public static Player getNextPlayer(BoardStatus board) {
        int p1Count = board.getPlayerCount(Player.Player_1);
        int p2Count = board.getPlayerCount(Player.Player_2);

        if (p1Count == p2Count)
            return Player.Player_1;
        else
            return Player.Player_2;
    }

    public static boolean boardIsFull(BoardStatus board) {
        for (int y = 0; y < BoardStatus.NUMBER_OF_BOXES_PER_SIDE; ++y) {
            for (int x = 0; x < BoardStatus.NUMBER_OF_BOXES_PER_SIDE; ++x) {
                if (board.getBoxOwner(x, y) == Player.Unowned)
                    return false;
            }
        }
        return true;
    }
}
