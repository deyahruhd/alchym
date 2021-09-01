package jard.alchym.client.render.entity;

import dev.monarkhes.myron.api.Myron;
import jard.alchym.AlchymReference;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

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
    public void render(RevolverBulletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        BakedModel model = Myron.getModel (new Identifier (AlchymReference.MODID, "models/misc/bullet/ice_shard"));

        VertexConsumer consumer = vertexConsumers.getBuffer (RenderLayer.getSolid ());

        float smoothAge = (float) entity.age + tickDelta;
        Vec3d clientOffset = entity.getClientStartOffset (tickDelta);

        matrices.push ();
        matrices.translate (clientOffset.x, clientOffset.y, clientOffset.z);

        Vec3d vel = entity.getVelocity ().normalize ();
        float velYaw   = (float) Math.atan2 (vel.z, vel.x);
        float velPitch = (float) Math.acos (vel.y);
        matrices.multiply (Vec3f.POSITIVE_Y.getRadialQuaternion ((float) Math.PI * 1.5f - velYaw));
        matrices.multiply (Vec3f.POSITIVE_X.getRadialQuaternion (- (velPitch + (float) Math.PI * 1.5f)));
        matrices.multiply (Vec3f.POSITIVE_Z.getRadialQuaternion ((entity.getUuid ().hashCode () / 1000.f) + smoothAge));

        matrices.scale (1.5f, 1.5f, 1.5f);

        MatrixStack.Entry transform = matrices.peek ();
        model.getQuads (null, null, entity.world.getRandom ()).forEach (quad -> consumer.quad (transform, quad, 1.f, 1.f, 1.f, light, 1));

        matrices.pop ();
    }


    @Override
    public Identifier getTexture (RevolverBulletEntity entity) {
        return null;
    }
}
