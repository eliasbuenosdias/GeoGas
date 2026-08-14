package com.eliasbuenosdias.geogas.utils.command;

/**
 * Interfaz del patrón Command.
 * <p>
 * Encapsula una acción reversible: {@link #execute()} aplica el cambio y
 * {@link #undo()} lo revierte.
 * </p>
 */
public interface Command {

    void execute();

    void undo();
}