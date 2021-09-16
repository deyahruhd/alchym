package jard.alchym.client.render.entity;

import dev.monarkhes.myron.api.Myron;
import jard.alchym.AlchymReference;
import jard.alchym.client.helper.RenderHelper;
import jard.alchym.entities.revolver.RevolverBulletEntity;
import jard.alchym.helper.MovementHelper;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

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
        /* main rocket */
        BakedModel model = Myron.getModel (new Identifier (AlchymReference.MODID, "models/misc/bullet/fire_rocket"));
        //BakedModel model = Myron.getModel (new Identifier (AlchymReference.MODID, "models/misc/bullet/ice_shard"));

        VertexConsumer consumer = vertexConsumers.getBuffer (RenderLayer.getCutout ());

        float smoothTime  = (float) entity.age + tickDelta;
        Vec3d clientOffset = entity.getClientStartOffset (tickDelta);

        matrices.push ();
        matrices.translate (clientOffset.x, clientOffset.y, clientOffset.z);

        Vec3d vel = entity.getVelocity ().normalize ();
        float velYaw   = (float) Math.atan2 (vel.z, vel.x);
        float velPitch = (float) Math.acos (vel.y);

        matrices.multiply (Vec3f.POSITIVE_Y.getRadialQuaternion ((float) Math.PI * 1.5f - velYaw));
        matrices.multiply (Vec3f.POSITIVE_X.getRadialQuaternion (- (velPitch + (float) Math.PI * 1.5f)));

        Vec3d sway = entity.getClientSway (tickDelta);
        matrices.translate (sway.x, sway.y, sway.z);

        matrices.multiply (Vec3f.POSITIVE_Z.getRadialQuaternion (entity.seed + smoothTime / -10.f));

        matrices.scale (1.5f, 1.5f, 1.5f);

        MatrixStack.Entry transform = matrices.peek ();
        model.getQuads (null, null, entity.world.getRandom ()).forEach (quad -> consumer.quad (transform, quad, 1.f, 1.f, 1.f, (light + 128), 1));

        // fire plumes
        if (entity.age > 2) {
            float plumeAmount = (float) net.minecraft.util.math.MathHelper.clamp (Math.tanh ((smoothTime - 1.f) / 4.f), 0.f, 1.f);

            VertexConsumer plumeConsumer = vertexConsumers.getBuffer (RenderLayer.getEntityAlpha (new Identifier (AlchymReference.MODID, "textures/bullet/fire_rocket_plumes.png")));

            Matrix4f invertedModel = transform.getModel ().copy ();
            invertedModel.invert ();

            Vector4f transformedEye = new Vector4f (0.f, 0.f, 0.f, 1.f);
            transformedEye.transform (invertedModel);
            Vec3d eye = new Vec3d (transformedEye.getX (), transformedEye.getY (), transformedEye.getZ ()).multiply (- 1.);

            Vec3d start = new Vec3d (0.f, 0.f, 0.0825f);
            for (int i = 0; i < 8; ++ i) {
                double angle = i * Math.PI * 0.4;

                if (i >= 5) {
                    angle = i * Math.PI * (0.35 + Math.sin (entity.seed * i) * 0.10);
                    angle *= angle;
                }
                Vec3d cycle = new Vec3d (Math.sin (angle), Math.cos (angle), 0.0);

                Vec3d plumeStart = start.add (cycle.multiply (0.015625));
                Vec3d dir = new Vec3d (cycle.x, cycle.y, 0.75).normalize ().multiply (0.1875 * plumeAmount);

                float originU = (18.f + 7.f * (i % 2)) / 32.f;
                float originV = (7.f * (i / 2)) / 256.f;

                originV += ((entity.age % 16) / 2) * 0.125f;

                RenderHelper.axisAlignedQuad (plumeConsumer,
                        matrices, plumeStart, dir, eye, 0.1875f * 0.857143f,
                        new Vec3i (255, 255, 255), 255,
                        originU, originV, 7.f / 32.f, 7.f / 256.f);
            }
        }
        //*/

        matrices.pop ();
    }


    @Override
    public Identifier getTexture (RevolverBulletEntity entity) {
        return null;
    }
}
