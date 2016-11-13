package org.c8.research.comming

import org.c8.research.comming.adapters.AvatarChooserAdapter

object Constants {
    val Avatars = arrayOf(
            AvatarChooserAdapter.Avatar("ava1", R.drawable.man1),
            AvatarChooserAdapter.Avatar("ava2", R.drawable.man2),
            AvatarChooserAdapter.Avatar("ava3", R.drawable.man3),
            AvatarChooserAdapter.Avatar("ava4", R.drawable.man4)
    )
    val AvatarsMap = Avatars.associateBy({ it.id }, { it.drawableResource })
}
