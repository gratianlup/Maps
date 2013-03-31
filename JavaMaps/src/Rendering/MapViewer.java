// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "JavaMaps" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "JavaMaps" nor
// may "JavaMaps" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package Rendering;
import Rendering.Animations.*;
import java.util.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.*;
import Core.*;
import Rendering.Renderers.*;
import Rendering.Utils.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.DecimalFormat;

public class MapViewer extends JPanel implements IAnimatable, IRendererHost {
    private static final long ZOOM_DURATION = 500;
    private static final long PAN_DURATION = 1000;

    // Used to sort the drawing modules based on their ZIndex.
    class  RendererComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            IRenderer renderer1 = (IRenderer)o1;
            IRenderer renderer2 = (IRenderer)o2;
            return renderer1.ZIndex() - renderer2.ZIndex();
        }
    }

    // Modifies the map view when the control is resized by the user.
    class ResizeListener implements ComponentListener {
        public void componentResized(ComponentEvent evt) {
            SetSize(getSize().width, getSize().height);
        }

        public void componentMoved(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
        public void componentHidden(ComponentEvent e) {}
    }

    // Receives and handles the mouse events.
    class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(!loaded_) {
                return;
            }
            
            // First send the event to all controls.
            Point mousePosition = new Point(e.getX(), e.getY());
            Modifier modifier = GetModifiers(e);
            
            if(controls_.MouseDown(mousePosition, view_, modifier)) {
                return;
            }

            Point adjPosition = AdjustedPosition(e);
            SendMouseDown(adjPosition, modifier); // Trimite mesajul.
            
            if(!MouseCaptured()) {
                // If the mouse has not been captured by one 
                // of the controls below it start moving the map view.
                startPosition_ = adjPosition;
                lastPosition_ = adjPosition;
                draggingStart_ = System.currentTimeMillis();
                dragging_ = true;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(!loaded_) {
                return;
            }

            if(e.getClickCount() >= 2) {
                // Zoom in/out when doing a double-click.
                Point origin = new Point(e.getX(), e.getY());
                Modifier modifier = GetModifiers(e);
                
                if(modifier.IsSet(Modifier.BUTTON_LEFT)) {
                    Zoom(1.0, origin, ZOOM_DURATION); // Zoom in.
                }
                else {
                    Zoom(-1.0, origin, ZOOM_DURATION); // Zoom out.
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if(!loaded_) {
                return;
            }

            Point mousePosition = new Point(e.getX(), e.getY());
            Modifier modifier = GetModifiers(e);
            
            if(controls_.MouseMoved(mousePosition, view_, modifier)) {
                return;
            }

            Point adjPosition = AdjustedPosition(e);
            SendMouseMoved(adjPosition, modifier); // Trimite mesajul.
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if(!loaded_) {
                return;
            }

            Point mousePosition = new Point(e.getX(), e.getY());
            Modifier modifier = GetModifiers(e);
            
            if(controls_.MouseDragged(mousePosition, view_, modifier)) {
                return;
            }

            Point adjPosition = AdjustedPosition(e);
            SendMouseDragged(adjPosition, modifier);

            if(dragging_) {
                // The map view is being moved.
                long time = System.currentTimeMillis() - draggingStart_;
                
                if(time > 250) {
                    // At a predefined interval the start positon is reset
                    // to compute an updated acceleration value.
                    startPosition_ = adjPosition;
                    draggingStart_ = System.currentTimeMillis();
                }

                // Stop the old view moving animation if still active.
                if(panAnim_ != null) {
                    RemoveAnimation(panAnim_);
                }

                Point delta = new Point(lastPosition_.X() - adjPosition.X(),
                                        lastPosition_.Y() - adjPosition.Y());
                lastPosition_ = adjPosition;
                view_.Bounds().Offset(delta);
                LimitBounds(view_);
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(!loaded_) {
                return;
            }

            Point mousePosition = new Point(e.getX(), e.getY());
            Modifier modifier = GetModifiers(e);
            
            if(controls_.MouseUp(mousePosition, view_, modifier)) {
                return;
            }

            Point adjPosition = AdjustedPosition(e);
            SendMouseUp(adjPosition, modifier); // Trimite mesajul.
            
            if(dragging_) {
                // Check if an animation that moves the view a little bit more
                // should be added when the user stops dragging.
                dragging_ = false;
                double time = System.currentTimeMillis() - draggingStart_;
                
                if(time > 300) {
                    // If the user didn't move the mouse lately
                    // don't start the animation, it would look strange.
                    return;
                }

                // Compute the acceleration based on the distance
                // covered in the elapsed time.
                double dx = startPosition_.X() - adjPosition.X();
                double dy = startPosition_.Y() - adjPosition.Y();
                double accelX = dx / time;
                double accelY = dy / time;

                // Start the animation only if the acceleration is high enough.
                // These values could be modified to find a more pleasing effect.
                if(Math.abs(accelX) > 0.25 || Math.abs(accelY) > 0.25) {
                    dx = Math.abs(dx) * accelX * 4;
                    dy = Math.abs(dy) * accelY * 4;

                    View temp = new View();
                    temp.SetMaxBounds(view_.MaxBounds());
                    temp.SetViewBounds(view_.ViewBounds());
                    temp.SetBounds(new Region2D(view_.Bounds().Left() + dx,
                                                view_.Bounds().Top() + dy,
                                                view_.Bounds().Width(),
                                                view_.Bounds().Height()));
                    LimitBounds(temp);
                    dx = temp.Bounds().Left() - view_.Bounds().Left();
                    dy = temp.Bounds().Top() - view_.Bounds().Top();
                    ExpInterpolation.Mode mode = ExpInterpolation.Mode.EaseOut;

                    // Stop the old animation if still active.
                    if(panAnim_ != null) {
                        RemoveAnimation(panAnim_);
                    }

                    // Create and start the new animation.
                    panAnim_ = new PanAnimation((IAnimatable)e.getComponent(),
                                                 view_, dx, dy, PAN_DURATION,
                                                 new ExpInterpolation(3, mode));
                    AddAnimation(panAnim_);
                }
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(!loaded_) {
                return;
            }

            // Check how the scrool whell has been rotated.
            Point origin = new Point(e.getX(), e.getY());

            if(e.getUnitsToScroll() > 0) {
                Zoom(-1.0, origin, ZOOM_DURATION); // Zoom out.
            }
            else {
                Zoom(1.0, origin, ZOOM_DURATION); // Zoom in.
            }
        }
    }

    // Forces the control to update the animation state
    // and ultimately redraw the modified screen regions.
    class AnimationUpdater implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            UpdateAnimations();

            // Stop the timer when there are no animations.
            if(animations_.isEmpty()) {
                ((javax.swing.Timer)e.getSource()).stop();
            }
        }
    }


    /*
     ** Members.
     */
    private IViewerHost host_;
    private ArrayList<IRenderer> renderers_;
    private ArrayList<IPrefetcher> prefetchers_;
    private ArrayList<AnimationBase> animations_;
    private IMapProvider provider_;
    private Region2D[] mapBounds_;
    private View view_;
    private int zoomLevels_;
    private IProjection projection_;
    private boolean buffersInvalidated_;
    private boolean dragging_;
    private Point lastPosition_;
    private Point startPosition_;
    private long draggingStart_;
    javax.swing.Timer animationTimer_;
    PanAnimation panAnim_;
    ZoomAnimation zoomAnim_;
    long lastPanCommand_;
    Point lastPanDelta_;
    HashMap<IRenderer, AnimationBase> layerAnimations_;
    Font debugFont_;
    boolean showDebug_;
    OverlayRenderer overlay_;
    MapControls controls_;
    boolean loaded_;

    /*
     ** Constructors.
     */
    public MapViewer(IViewerHost host) {
        assert(host != null);
        // ------------------------------------------------
        host_ = host;
        renderers_ = new ArrayList<IRenderer>(16);
        prefetchers_ = new ArrayList<IPrefetcher>(4);
        animations_ = new ArrayList<AnimationBase>(16);
        layerAnimations_ = new HashMap<IRenderer, AnimationBase>(8);
        debugFont_ = new Font("Dialog", Font.PLAIN, 14);
    }

    /*
     ** Public methods.
     */
    public void LoadMap(IMapProvider provider) {
        assert(provider != null);
        assert(provider.ZoomLevels() > 0);
        // ------------------------------------------------
        renderers_.clear();
        prefetchers_.clear();
        animations_.clear();
        layerAnimations_.clear();

        provider_ = provider;
        projection_ = provider.Projection();
        zoomLevels_ = provider.ZoomLevels();

        // Add the predefined layers.
        overlay_ = new OverlayRenderer(this);
        renderers_.add(overlay_);
        controls_ = new MapControls(this);
        renderers_.add(controls_);

        // Set the maximum bounds of the map.
        mapBounds_ = new Region2D[zoomLevels_];
        
        for(int i = 0; i < zoomLevels_; i++) {
            mapBounds_[i] = provider_.MapBounds(i);
        }

        // Enumarate all layers defined by the map
        // and add the corresponding render modules to the view.
        Iterator<ILayer> layerIt = provider.GetLayerIterator();
        while(layerIt.hasNext()) {
            ILayer layer = layerIt.next();
            IRenderer renderer = RendererFactory.Create(layer, this);
            renderers_.add(renderer);

            // Check if the render module implements prefetching,
            // and if it does register it for frequent notifications.
            if(renderer.HasPrefetcher()) {
                prefetchers_.add(renderer.Prefetcher());
            }
        }

        // The layers are sorted based on their predefined ZIndex.
        SortRenderers();

        // Set the initial map view.
        view_ = new View();
        view_.SetZoom(0); // Se afiseaza de la nivelul cel mai mic.
        view_.SetMaxBounds(provider_.MapBounds((int)view_.Zoom()));
        view_.SetBounds(mapBounds_[(int)view_.Zoom()]);
        buffersInvalidated_ = true;

        // Add various event listeneres.
        MouseHandler mouseHandler = new MouseHandler();
        this.addComponentListener(new ResizeListener());
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addMouseWheelListener(mouseHandler);
        SetSize(this.getSize().width, this.getSize().height);
        loaded_ = true;
    }

    public IMapProvider MapProvider() { 
        return provider_; 
    }

    public Action SendAction(Rendering.Action action) {
        host_.ActionPerformed(action);
        return action;
    }

    public void AddRenderer(IRenderer renderer) {
        assert(renderer != null);
        // ------------------------------------------------
        renderers_.add(renderer);
        SortRenderers();
        repaint();
    }

    public void AddLayer(ILayer layer) {
        assert(layer != null);
        // ------------------------------------------------
        renderers_.add(RendererFactory.Create(layer, this));
        SortRenderers();
        repaint();
    }

    public void RemoveRenderer(IRenderer renderer) {
        assert(renderer != null);
        // ------------------------------------------------
        renderers_.remove(renderer);
        repaint();
    }

    public void RemoveLayer(ILayer layer) {
        assert(layer != null);
        // ------------------------------------------------
        for(int i = 0; i < renderers_.size(); i++) {
            if(renderers_.get(i).Layer() == layer) {
                renderers_.remove(i);
                repaint();
                break;
            }
        }
    }

    public IRenderer GetRenderer(ILayer layer) {
        assert(layer != null);
        // ------------------------------------------------
        for(int i = 0; i < renderers_.size(); i++) {
            if(renderers_.get(i).Layer().equals(layer)) {
                return renderers_.get(i);
            }
        }

        return null;
    }

    public Iterator<IRenderer> Renderers() { 
        return renderers_.iterator(); 
    }

    public void ShowLayer(IRenderer renderer, long duration) {
        assert(renderer != null);
        // ------------------------------------------------
        if(duration > 0) {
            double start = 0.0;
            double end = 1.0;

            // Check if there is an animation assciated with the renderer.
            // In case an animation is stil running it is stopped.
            LayerAnimation layerAnim = (LayerAnimation)layerAnimations_.get(renderer);
            
            if(layerAnim != null) {
                start = layerAnim.Opacity();
                layerAnimations_.remove(renderer);
                RemoveAnimation(layerAnim);
            }

            // Create and start the new animation.
            layerAnim = new LayerAnimation(this, renderer, start, end, duration,
                                           new LinearInterpolation());
            AddAnimation(layerAnim);
            layerAnimations_.put(renderer, layerAnim);
        }
        else {
            renderer.SetVisible(true);
            repaint();
        }
    }

    public void ShowLayer(ILayer layer, long duration) {
        assert(layer != null);
        // -----------------------------------
        ShowLayer(GetRenderer(layer), duration);
    }

    public void HideLayer(IRenderer renderer, long duration) {
        assert(renderer != null);
        // ------------------------------------------------
        if(duration > 0) {
            double start = 1.0;
            double end = 0.0;

            // Check if there is an animation assciated with the renderer.
            // In case an animation is stil running it is stopped.
            LayerAnimation layerAnim = (LayerAnimation)layerAnimations_.get(renderer);
            
            if(layerAnim != null) {
                start = layerAnim.Opacity();
                layerAnimations_.remove(renderer);
                RemoveAnimation(layerAnim);
            }

            // Create and start the new animation.
            layerAnim = new LayerAnimation(this, renderer, start, end, duration,
                                           new LinearInterpolation());
            AddAnimation(layerAnim);
            layerAnimations_.put(renderer, layerAnim);
        }
        else {
            renderer.SetVisible(false);
            repaint();
        }
    }

    public void HideLayer(ILayer layer, long duration) {
        assert(layer != null);
        // -----------------------------------
        HideLayer(GetRenderer(layer), duration);
    }

    public void Pan(Core.Point delta, long duration) {
        // Stop any panning animation that might be running.
        if(panAnim_ != null) {
            if(lastPanDelta_.equals(delta) &&
               (System.currentTimeMillis() - lastPanCommand_) < panAnim_.Duration()) {
                return;
            }
            
            RemoveAnimation(panAnim_);
            panAnim_ = null;
        }

        // Remember when the panning started.
        lastPanDelta_ = delta;
        lastPanCommand_ = System.currentTimeMillis();

        if(duration > 0) {
            // Use an animation to do the panning.
            // Compute the view for the panning end position.
            View temp = new View();
            temp.SetMaxBounds(view_.MaxBounds());
            temp.SetViewBounds(view_.ViewBounds());
            temp.SetBounds(new Region2D(view_.Bounds().Left() + delta.X(),
                                        view_.Bounds().Top() + delta.Y(),
                                        view_.Bounds().Width(),
                                        view_.Bounds().Height()));
            LimitBounds(temp);
            double dx = temp.Bounds().Left() - view_.Bounds().Left();
            double dy = temp.Bounds().Top() - view_.Bounds().Top();
            ExpInterpolation.Mode mode = ExpInterpolation.Mode.EaseInOut;

            long durationX = Math.max(100, (long)(duration * (dx / delta.X())));
            long durationY = Math.max(100, (long)(duration * (dy / delta.Y())));
            
            // Create and start the animation.
            panAnim_ = new PanAnimation(this, view_, dx, dy, 
                                        Math.max(durationX, durationY),
                                        new ExpInterpolation(2, mode));
            AddAnimation(panAnim_);
        }
        else {
            // Do the panning without any animation.
            view_.Bounds().Offset(delta);
            LimitBounds(view_);
            repaint();
        }
    }

    private View ZoomTarget(double destZoom, Core.Point origin) {
        // Limit the zoom to the maximum allowed level.
        destZoom = Math.max(0, Math.min(destZoom, provider_.ZoomLevels() - 1));
        
        // Compute the size of the new region in the view.
        Region2D dstBounds = provider_.MapBounds((int)Math.floor(destZoom));
        Region2D nextBounds = provider_.MapBounds((int)Math.ceil(destZoom));
        double oldWidth = view_.MaxBounds().Width() + view_.LeftMargin();
        double oldHeight = view_.MaxBounds().Height() + view_.TopMargin();

        double newWidth = dstBounds.Width() + 
                          ((nextBounds.Width() - dstBounds.Width()) * (destZoom - Math.floor(destZoom)));
        double newHeight = dstBounds.Height() +
                          ((nextBounds.Height() - dstBounds.Height()) * (destZoom - Math.floor(destZoom)));

        // Compute the displacement required to keep
        // the origin point at the same position in the view.
        double dx = (newWidth - oldWidth) * (origin.X() / oldWidth);
        double dy = (newHeight - oldHeight) * (origin.Y() / oldHeight);

        // Set the new view region.
        View temp = new View();
        temp.SetMaxBounds(view_.MaxBounds());
        temp.SetViewBounds(view_.ViewBounds());
        temp.SetBounds(new Region2D(view_.Bounds()));
        temp.SetZoom(destZoom);
        temp.SetMaxBounds(new Region2D(0, 0, newWidth, newHeight));

        Region2D bounds = temp.Bounds();
        Region2D viewBounds = temp.ViewBounds();
        bounds.Offset(dx, dy);
        bounds.SetWidth(Math.min(viewBounds.Width(), newWidth));
        bounds.SetHeight(Math.min(viewBounds.Height(), newHeight));
        LimitBounds(temp);
        return temp;
    }

    private void ZoomTo(View target) {
        view_.SetBounds(target.Bounds());
        view_.SetMaxBounds(target.MaxBounds());
        view_.SetZoom(target.Zoom());
        view_.SetLeftMargin(target.LeftMargin());
        view_.SetTopMargin(target.TopMargin());
        repaint();
    }

    public void Zoom(double amount, long duration) {
        // The zoom point is in the middle of the current view.
        Region2D bounds = view_.ViewBounds();
        Point origin = new Core.Point(bounds.Width() / 2, bounds.Height() / 2);
        Zoom(amount, origin, duration);
    }

    public void Zoom(double amount, Point origin, long duration) {
        assert(duration >= 0);
        StopPanning();
        StopZooming();

        // Adjust the origin coordinates.
        origin.SetX(origin.X() + view_.Bounds().Left() - view_.LeftMargin());
        origin.SetY(origin.Y() + view_.Bounds().Top() - view_.TopMargin());

        amount = Math.rint(amount); // Round the zoom level to the nearest integer.
        double destZoom = view_.Zoom() + amount;
        
        if(amount >= 0) {
            destZoom = Math.ceil(destZoom);
        }
        else {
            destZoom = Math.floor(destZoom);
        }

        destZoom = Math.max(0, Math.min(destZoom, provider_.ZoomLevels() - 1));
        double origXPerc = origin.X() / view_.MaxBounds().Width();
        double origYPerc = origin.Y() / view_.MaxBounds().Height();

        // Create and start the animation.
        ExpInterpolation.Mode mode = ExpInterpolation.Mode.EaseOut;
        zoomAnim_ = new ZoomAnimation(this, view_, origXPerc, origYPerc, destZoom,
                                      duration, new ExpInterpolation(3, mode));
        AddAnimation(zoomAnim_);
    }

    public void ApplyZoomStep(double origXPerc, double origYPerc,
                              double destZoom) {
        Region2D bounds = view_.MaxBounds();
        Point origin = new Core.Point((bounds.Left() + bounds.Width() - 
                                       view_.LeftMargin()) * origXPerc,
                                      (bounds.Top() + bounds.Height() - 
                                       view_.TopMargin()) * origYPerc);
        View target = ZoomTarget(destZoom, origin);
        ZoomTo(target);
    }


    public void ShowRegion(Region region) {
        // TODO
    }

    public void ShowCoordinates(Coordinates coord) {
        // TODO
    }

    public IObjectInfo HitTest(Core.Point point) {
        // Send the hit test message to all layers,
        // in the order defined by their ZIndex.
        int count = renderers_.size();
        
        for(int i = count - 1; i >= 0; i--) {
            IObjectInfo result = renderers_.get(i).HitTest(point, view_);
            
            if(result != null) {
                return result;
            }
        }

        return null;
    }

    public void SetSize(double width, double height) {
        try {
            view_.SetViewBounds(new Region2D(0, 0, width, height));

            Region2D prevBounds = view_.Bounds();
            Region2D bounds = new Region2D(prevBounds.Left(), prevBounds.Top(),
                                           Math.min(width, view_.MaxBounds().Width()),
                                           Math.min(height, view_.MaxBounds().Height()));
            view_.SetBounds(bounds);
            LimitBounds(view_);

            // Update the position of the controls.
            if(controls_ != null) {
                controls_.ViewerResized(view_);
            }

            buffersInvalidated_ = true;
            CreateBuffers();
            repaint();
        }
       catch(Exception e) {
           // TODO
       }
    }

    public void AddAnimation(AnimationBase animation) {
        animations_.add(animation);
        animation.Start();

        // Start the timer if this is the first animation.
        if(animations_.size() == 1) {
            StartTimer();
        }
    }

    public void RemoveAnimation(AnimationBase animation) {
        animations_.remove(animation);

        // Stop the timer if this was the last animation.
        if(animations_.isEmpty()) {
            StopTimer();
        }
    }

    public IOverlayHost Overlay() {
        return overlay_;
    }

    public void Repaint() {
        repaint();
    }

    public void SetDebug(boolean value) {
        showDebug_ = value;
        repaint();
    }

    /*
     ** Private methods.
     */
    private void SortRenderers() {
        // Keep the rendering modules sorted by their ZIndex.
        Collections.sort(renderers_, new RendererComparator());
    }

    private void CreateBuffers() {
        // Create the buffers where the drawing is d0one.
        if(buffersInvalidated_) {
            view_.CreateBuffers(getGraphicsConfiguration());
            buffersInvalidated_ = false;
        }
    }

    private void StartTimer() {
        // Create a new time if not already created.
        // The timer resolution is enough to have smooth animations.
        if(animationTimer_ == null) {
            animationTimer_ = new javax.swing.Timer(16, new AnimationUpdater());
            animationTimer_.start();
        }
        else {
            animationTimer_.restart();
        }
    }

    private void StopTimer() {
        assert(animationTimer_ != null);
        // ------------------------------------------------
        animationTimer_.stop();
    }

    private void LimitBounds(View view) {
        // Limit the view to a region valid for the current zoom level.
        Region2D bounds = view.Bounds();
        Region2D viewBounds = view.ViewBounds();

        // Handle the case when the map is smaller than the control.
        if(bounds.Width() < viewBounds.Width()) {
            view.SetLeftMargin(viewBounds.Width() / 2 - bounds.Width() / 2);
        }
        else {
            view.SetLeftMargin(0);
        }

        if(bounds.Height() < viewBounds.Height()) {
            view.SetTopMargin(viewBounds.Height() / 2 - bounds.Height() / 2);
        }
        else {
            view.SetTopMargin(0);
        }

        // Don't let the map to be moved above the bottop-right edge.
        if(bounds.Right() > view.MaxBounds().Right()) {
            bounds.SetLeft(view.MaxBounds().Right() - bounds.Width());
        }

        if(bounds.Bottom() > view.MaxBounds().Bottom()) {
            bounds.SetTop(view.MaxBounds().Bottom() - bounds.Height());
        }

        bounds.SetLeft(Math.max(0, bounds.Left()));
        bounds.SetTop(Math.max(0, bounds.Top()));
    }

    private void StopPanning() {
        if(panAnim_ != null) {
            RemoveAnimation(panAnim_);
            panAnim_ = null;
        }
    }

    private void StopZooming() {
        if(zoomAnim_ != null) {
            RemoveAnimation(zoomAnim_);
            zoomAnim_ = null;
        }
    }

    private void UpdateAnimations() {
        // Updates the state of all animations, notifying the parent
        // when one of them completes so they can be removed.
        if(animations_.isEmpty()) {
            return;
        }

        for(int i = 0; i < animations_.size(); i++) {
            AnimationBase animation = animations_.get(i);
            animation.Update();

            if(animation.Completed()) {
                animation.Parent().AnimationCompleted(animation);
                animations_.remove(i);
                i--;
            }
        }

        // Force a repaint to view the updated map.
        repaint();
    }

    public void AnimationCompleted(AnimationBase animation) {
        // This method is called when an animation completed.
        if(animation == panAnim_) {
            panAnim_ = null;
        }
        else if(animation == zoomAnim_) {
            zoomAnim_ = null;
        }

        if(animation.getClass().equals(LayerAnimation.class)) {
            LayerAnimation layerAnim = (LayerAnimation)animation;
            layerAnimations_.remove(layerAnim.Renderer());
        }
    }

    private Modifier GetModifiers(MouseEvent e) {
        Modifier modifier = new Modifier();

        if((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            modifier.Set(Modifier.BUTTON_RIGHT);
        }
        if((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            modifier.Set(Modifier.BUTTON_LEFT);
        }
        if((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0) {
            modifier.Set(Modifier.BUTTON_MIDDLE);
        }
        if((e.getModifiers() & MouseEvent.ALT_MASK) != 0) {
            modifier.Set(Modifier.KEY_ALT);
        }
        if((e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
            modifier.Set(Modifier.KEY_CTRL);
        }
        if((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0) {
            modifier.Set(Modifier.KEY_SHIFT);
        }

        return modifier;
    }

    private void SendMouseDown(Point point, Modifier modifier) {
        // Send a mouse-down event to all render modules.
        int count = renderers_.size();
        
        for(int i = count - 1; i >= 0; i--) {
            if(renderers_.get(i).Visible() &&
               renderers_.get(i).MouseDown(point, view_, modifier)) {
                return;
            }
        }
    }

    private void SendMouseUp(Point point, Modifier modifier) {
        // Send a mouse-up event to all render modules.
        int count = renderers_.size();
        
        for(int i = count - 1; i >= 0; i--) {
            if(renderers_.get(i).Visible() &&
               renderers_.get(i).MouseUp(point, view_, modifier)) {
                return;
            }
        }
    }
    
    private void SendMouseMoved(Point point, Modifier modifier) {
        // Send a mouse-moved event to all render modules.
        int count = renderers_.size();
        
        for(int i = count - 1; i >= 0; i--) {
            if(renderers_.get(i).Visible() &&
               renderers_.get(i).MouseMoved(point, view_, modifier)) {
                return;
            }
        }
    }

    private void SendMouseDragged(Point point, Modifier modifier) {
        // Send a mouse-dragged event to all render modules.
        // This event is sent only to the module which already captured the mouse.
        int count = renderers_.size();
        
        for(int i = count - 1; i >= 0; i--) {
            IRenderer renderer = renderers_.get(i);
            
            if(renderers_.get(i).Visible() &&
               renderer.MouseCaptured()    && 
               renderers_.get(i).MouseDragged(point, view_, modifier)) {
                return;
            }
        }
    }

    private boolean MouseCaptured() {
        // Verify if any of the render modules captured the mouse.
        int count = renderers_.size();
        
        for(int i = count - 1; i >= 0; i--) {
            if(renderers_.get(i).MouseCaptured()) {
                return true;
            }
        }

        return false;
    }

    private Point AdjustedPosition(MouseEvent e) {
        return new Point(e.getX() - (int)view_.LeftMargin(),
                         e.getY() - (int)view_.TopMargin());
    }

    private void DisplayDebugInfo(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(-1, getHeight() - 200, 350, 180);
        g.setColor(Color.BLACK);
        g.drawRect(-1, getHeight() - 200, 350, 180);
        int y = getHeight() - 180;

        DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
        g.drawString("Zoom: " + oneDigit.format(view_.Zoom()), 10, y);
        y += 22;
        g.drawString("Map Bounds: " + view_.Bounds().toString(), 10, y);
        y += 22;
        g.drawString("View Bounds: " + view_.ViewBounds().toString(), 10, y);
        y += 22;
        g.drawString("Max. Bounds: " + view_.MaxBounds().toString(), 10, y);
        y += 22;
        g.drawString("Animations: " + animations_.size(), 10, y);
        y += 22;
        g.drawString("Renderers: " + renderers_.size(), 10, y);
        y += 22;

        int visibleCount = 0;
        for(IRenderer rend : renderers_) {
            if(rend.Visible()) {
                visibleCount++;
            }
        }
        
        g.drawString("Visible rend.: " + visibleCount, 10, y);
        y += 22;
        g.drawString("Prefetchers: " + EnabledPrefetchers(), 10, y);
    }

    private void UpdatePrefetchers() {
        for(IPrefetcher prefetcher : prefetchers_) {
            prefetcher.ViewChanged(view_);
        }
    }

    private int EnabledPrefetchers() {
        int ct = 0;
        for(IPrefetcher prefetcher : prefetchers_) {
            if(prefetcher.Enabled()) {
                ct++;
            }
        }
        
        return ct;
    }

    public void EnablePrefetchers(boolean state) {
        for(IPrefetcher prefetcher : prefetchers_) {
            prefetcher.SetEnabled(state);
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if((MapProvider() == null) ||
           (view_.ViewBounds() == null)) {
            return;
        }

        // Update the current view and notify the prefetchers.
        LimitBounds(view_);
        view_.ComputeAdjacentBounds(mapBounds_);
        UpdatePrefetchers();

        // Redraw the entire map (rendering modules + controls).
        Graphics2D g = (Graphics2D)graphics;

        do {
            // Check if the drawing buffers are still valid
            // (they are invalidated, for example, when the window that
            //  hosts the control is moved to another display).
            view_.ValidateBuffers(getGraphicsConfiguration());

            // Fill the background with a solid color,
            // then ask each rendering module to draw itself.
            g.clearRect(0, 0, (int)view_.ViewBounds().Width(),
                              (int)view_.ViewBounds().Height());

            int count = renderers_.size();
            
            for(int i = 0; i < count; i++) {
                IRenderer renderer = renderers_.get(i);
                if(renderer.Visible()) {
                    renderer.Render(view_);
                }
            }

            // Copy the buffer to the actual control.
            view_.Render(g, this);
            
            if(showDebug_) {
                DisplayDebugInfo(g);
            }

            // Render the controls above all render modules.
            controls_.Render(g, view_);
        } while(view_.DrawingFailed());
    }
}
