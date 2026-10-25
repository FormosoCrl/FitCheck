package com.dformoso.fitcheck.model;

/**
 * Una version publicada de la app.
 *
 * <p>La comparacion se hace con {@code versionCode} y nunca con {@code versionName}:
 * "1.10" es posterior a "1.9" pero al compararlos como texto sale al reves.
 *
 * @param versionCode numero que sube en cada publicacion
 * @param versionName lo que se le ensena a la persona
 * @param notes       que cambia en esta version
 * @param apkUrl      de donde se descarga
 * @param sizeBytes   tamano, para avisar antes de gastar datos. 0 si no se sabe.
 */
public record AppUpdate(
        int versionCode,
        String versionName,
        String notes,
        String apkUrl,
        long sizeBytes) {

    /** Si esta version es mas nueva que la instalada. */
    public boolean isNewerThan(int installedVersionCode) {
        return versionCode > installedVersionCode;
    }

    /** Tamano en MB con un decimal, para el texto del aviso. */
    public double sizeMb() {
        return sizeBytes / (1024d * 1024d);
    }
}
