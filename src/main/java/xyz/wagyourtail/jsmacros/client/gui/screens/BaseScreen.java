package xyz.wagyourtail.jsmacros.client.gui.screens;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.client.gui.overlays.IOverlayParent;
import xyz.wagyourtail.jsmacros.client.gui.overlays.OverlayContainer;

public abstract class BaseScreen extends Screen implements IOverlayParent {
    protected Screen parent;
    protected OverlayContainer overlay;

    protected BaseScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    public void reload() {
        init();
    }

    protected void init() {
        assert client != null;
        super.init();
        buttons.clear();
        children.clear();
        overlay = null;
        JsMacros.prevScreen = this;
        client.keyboard.setRepeatEvents(true);
    }

    public void removed() {
        assert client != null;
        client.keyboard.setRepeatEvents(false);
    }
    
    public void openOverlay(OverlayContainer overlay) {
        openOverlay(overlay, true);
    }
    @Override
    public IOverlayParent getFirstOverlayParent() {
        return this;
    }
    
    @Override
    public OverlayContainer getChildOverlay() {
        if (overlay != null) return overlay.getChildOverlay();
        return null;
    }
    
    public void openOverlay(OverlayContainer overlay, boolean disableButtons) {
        if (disableButtons) {
            for (AbstractButtonWidget b : buttons) {
                overlay.savedBtnStates.put(b, b.active);
                b.active = false;
            }
        }
        this.overlay = overlay;
        overlay.init();
    }

    @Override
    public void closeOverlay(OverlayContainer overlay) {
        if (overlay == null) return;
        for (AbstractButtonWidget b : overlay.getButtons()) {
            removeButton(b);
        }
        for (AbstractButtonWidget b : overlay.savedBtnStates.keySet()) {
            b.active = overlay.savedBtnStates.get(b);
        }
        if (this.overlay == overlay) this.overlay = null;
    }

    @Override
    public void removeButton(AbstractButtonWidget btn) {
        buttons.remove(btn);
        children.remove(btn);
    }
    
    @Override
    public <T extends AbstractButtonWidget> T addButton(T button) {
        return super.addButton(button);
    }
    
    @Override
    public void setFocused(@Nullable Element focused) {
        super.setFocused(focused);
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (overlay != null) {
                this.overlay.closeOverlay(this.overlay.getChildOverlay());
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (overlay!= null && overlay.scroll != null) overlay.scroll.mouseDragged(mouseX, mouseY, 0, 0, -amount * 2);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (overlay != null) overlay.onClick(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (overlay != null) overlay.render(matrices, mouseX, mouseY, delta);
    }

    public boolean shouldCloseOnEsc() {
        return this.overlay == null;
    }

    public void onClose() {
        assert client != null;
        if (client.world == null)
            openParent();
        else {
            setFocused(null);
            client.openScreen(null);
        }
    }
    
    public void openParent() {
        assert client != null;
        client.openScreen(parent);
    }
}