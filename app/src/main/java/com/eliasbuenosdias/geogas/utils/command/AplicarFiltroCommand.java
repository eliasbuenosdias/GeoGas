package com.eliasbuenosdias.geogas.utils.command;

import com.eliasbuenosdias.geogas.utils.FiltrosEstado;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;

/**
 * Comando reversible que aplica un nuevo estado de filtros sobre un
 * {@link FiltrosManager}.
 * <p>
 * Antes de ejecutarse captura un snapshot del estado previo para poder
 * deshacer el cambio con {@link #undo()}.
 * </p>
 */
public class AplicarFiltroCommand implements Command {

    private final FiltrosManager filtros;
    private final FiltrosEstado estadoAnterior;
    private final FiltrosEstado estadoNuevo;
    private final Runnable alCambiar;

    /**
     * @param filtros      el gestor de filtros sobre el que actuar
     * @param estadoNuevo  el estado a aplicar al ejecutar el comando
     * @param alCambiar    callback invocado tras aplicar o deshacer el cambio
     */
    public AplicarFiltroCommand(FiltrosManager filtros, FiltrosEstado estadoNuevo, Runnable alCambiar) {
        this.filtros = filtros;
        this.estadoAnterior = filtros.snapshot();
        this.estadoNuevo = estadoNuevo;
        this.alCambiar = alCambiar;
    }

    @Override
    public void execute() {
        filtros.restore(estadoNuevo);
        if (alCambiar != null)
            alCambiar.run();
    }

    @Override
    public void undo() {
        filtros.restore(estadoAnterior);
        if (alCambiar != null)
            alCambiar.run();
    }
}