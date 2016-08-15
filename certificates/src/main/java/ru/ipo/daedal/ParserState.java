package ru.ipo.daedal;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 12:18.
 */
enum ParserState {

    ReadText, ReadSpaces, EscapeRead, ReadCommand, ReadExpression, Finished

}
