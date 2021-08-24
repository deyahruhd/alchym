package jard.alchym.client.render.entity;

import jard.alchym.entities.revolver.RevolverBulletEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

/***
 *  RevolverBulletEntityRenderer
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 22:10 on August 23, 2021.
 ***/
public class RevolverBulletEntityRenderer extends EntityRenderer<RevolverBulletEntity> {
    public RevolverBulletEntityRenderer (EntityRendererFactory.Context ctx) {
        super (ctx);
    }

    @Override
    public Identifier getTexture (RevolverBulletEntity entity) {
        return null;
    }
}
