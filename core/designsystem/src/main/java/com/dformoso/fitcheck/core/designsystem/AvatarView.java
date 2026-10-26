package com.dformoso.fitcheck.core.designsystem;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

/**
 * Foto de perfil, redonda.
 *
 * <p>Si no hay foto pinta un circulo con la inicial, como Gmail. El respaldo no es un
 * icono generico a proposito: en una lista de seguidores, veinte siluetas grises iguales
 * no dejan distinguir a nadie.
 */
public class AvatarView extends AppCompatImageView {

    public AvatarView(Context context) {
        this(context, null);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.CENTER_CROP);
    }

    /**
     * Pinta el avatar de esa persona.
     *
     * @param name lo que se usa para la inicial y el color de respaldo
     * @param url  foto, o null si no tiene
     */
    public void setUser(@Nullable String name, @Nullable String url) {
        InitialsDrawable fallback = new InitialsDrawable(name);

        if (url == null || url.isBlank()) {
            // Se cancela cualquier carga anterior: sin esto, en una lista reciclada la
            // foto de la fila que se fue puede aterrizar encima de esta.
            Glide.with(this).clear(this);
            setImageDrawable(fallback);
            return;
        }

        Glide.with(this)
                .load(url)
                .circleCrop()
                .placeholder(fallback)
                .error(fallback)
                .into(this);
    }
}
