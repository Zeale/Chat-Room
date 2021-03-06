package org.alixia.chatroom.api.guis;

import static org.alixia.chatroom.ChatRoom.WARNING_COLOR;

import org.alixia.chatroom.api.OS;
import org.alixia.chatroom.api.fx.tools.FXTools;
import org.alixia.chatroom.api.fx.tools.Resizable;
import org.alixia.chatroom.api.fx.tools.ResizeOperator;
import org.alixia.chatroom.impl.guis.chatroom.ChatRoomGUI;

import javafx.animation.FillTransition;
import javafx.animation.StrokeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ChatRoomWindow extends Stage {

	private static final Image DEFAULT_ICON = ChatRoomGUI.DEFAULT_ICON;

	public static final Color DEFAULT_WINDOW_BORDER_COLOR = new Color(0.2, 0.2, 0.2, 1),
			DEFAULT_NODE_OUTPUT_COLOR = new Color(0, 0, 0, 0.3), DEFAULT_NODE_ITEM_COLOR = Color.DARKGRAY,
			DEFAULT_WINDOW_BACKGROUND_COLOR = new Color(0.3, 0.3, 0.3, 0.8);

	{
		initStyle(StageStyle.TRANSPARENT);
		getIcons().add(DEFAULT_ICON);
	}

	protected final AnchorPane contentPane = new AnchorPane();
	protected final BorderPane root = new BorderPane(contentPane);
	protected final Scene scene = new Scene(root);

	{
		setScene(scene);
		scene.setFill(Color.TRANSPARENT);
		contentPane.setBackground(FXTools.getBackgroundFromColor(DEFAULT_WINDOW_BACKGROUND_COLOR));
	}

	private final StackPane close = new StackPane(), minimize = new StackPane(), expand = new StackPane();
	private final HBox menuBar = new HBox(OS.getOS() == OS.WINDOWS ? minimize : close, expand,
			OS.getOS() == OS.WINDOWS ? close : minimize);

	{

		final Color ITEM_COLOR = DEFAULT_NODE_ITEM_COLOR, BACKGROUND_COLOR = DEFAULT_WINDOW_BORDER_COLOR;

		close.setPrefSize(26, 26);
		minimize.setPrefSize(26, 26);
		expand.setPrefSize(26, 26);

		final Background background = new Background(new BackgroundFill(BACKGROUND_COLOR, null, null));
		close.setBackground(background);
		minimize.setBackground(background);
		expand.setBackground(background);

		final double size = 28;
		{
			if (OS.getOS() == OS.WINDOWS) {

				final double animationDuration = 0.2;

				{
					// Pos is the rect with a positive slope, neg is the negative sloped rect.
					Shape cross;
					{
						final Shape pos = new Rectangle(24, 2), neg = new Rectangle(24, 2);
						neg.setRotate(45);
						pos.setRotate(-45);
						cross = Shape.union(pos, neg);
					}

					cross.setFill(ITEM_COLOR);
					cross.setStroke(ITEM_COLOR);
					cross.setStrokeWidth(1);

					StackPane.setAlignment(cross, Pos.CENTER);

					close.getChildren().add(cross);

					final StrokeTransition stcross = new StrokeTransition(Duration.seconds(animationDuration), cross);
					final FillTransition ftcross = new FillTransition(Duration.seconds(animationDuration), cross);

					close.setOnMouseEntered(event -> {
						stcross.stop();
						ftcross.stop();

						stcross.setFromValue((Color) cross.getStroke());
						stcross.setToValue(Color.RED);
						ftcross.setFromValue((Color) cross.getFill());
						ftcross.setToValue(Color.RED);

						stcross.play();
						ftcross.play();

					});

					close.setOnMouseExited(event -> {
						stcross.stop();
						ftcross.stop();

						stcross.setFromValue((Color) cross.getStroke());
						stcross.setToValue(ITEM_COLOR);
						ftcross.setFromValue((Color) cross.getFill());
						ftcross.setToValue(ITEM_COLOR);

						stcross.play();
						ftcross.play();

					});
				}

				{
					// Expand/Maximize
					final Rectangle max = new Rectangle(20, 20);
					max.setFill(Color.TRANSPARENT);
					max.setStroke(ITEM_COLOR);
					max.setStrokeWidth(2.5);

					StackPane.setAlignment(max, Pos.CENTER);

					expand.getChildren().add(max);

					final StrokeTransition stexp = new StrokeTransition(Duration.seconds(animationDuration), max);

					expand.setOnMouseMoved(event -> {
						stexp.stop();

						stexp.setFromValue((Color) max.getStroke());
						stexp.setToValue(Color.GREEN);

						stexp.play();
					});

					expand.setOnMouseExited(event -> {
						stexp.stop();

						stexp.setFromValue((Color) max.getStroke());
						stexp.setToValue(ITEM_COLOR);

						stexp.play();
					});

				}

				{
					// Minimize
					final Rectangle min = new Rectangle(22, 2);
					StackPane.setAlignment(min, Pos.BOTTOM_CENTER);
					min.setFill(ITEM_COLOR);
					min.setStroke(ITEM_COLOR);
					min.setStrokeWidth(1);
					minimize.setPadding(new Insets(0, 0, 2, 0));
					minimize.getChildren().add(min);

					final StrokeTransition stmin = new StrokeTransition(Duration.seconds(animationDuration), min);
					final FillTransition ftmin = new FillTransition(Duration.seconds(animationDuration), min);

					minimize.setOnMouseEntered(event -> {
						stmin.stop();
						ftmin.stop();

						final Color darkGold = new Color(1, 190d / 255, 0, 1);
						stmin.setFromValue((Color) min.getStroke());
						stmin.setToValue(darkGold);
						ftmin.setFromValue((Color) min.getFill());
						ftmin.setToValue(darkGold);

						stmin.play();
						stmin.play();
					});

					minimize.setOnMouseExited(event -> {
						stmin.stop();
						ftmin.stop();

						stmin.setFromValue((Color) min.getStroke());
						stmin.setToValue(ITEM_COLOR);
						ftmin.setFromValue((Color) min.getFill());
						ftmin.setToValue(ITEM_COLOR);

						stmin.play();
						stmin.play();
					});

				}
			} else {
				final double animationDuration = 0.3;
				Shape closeFill, minimizeFill, expandFill;
				closeFill = new Circle(size / 5);
				minimizeFill = new Circle(size / 5);
				expandFill = new Circle(size / 5);
				closeFill.setFill(Color.CORAL);
				expandFill.setFill(WARNING_COLOR);
				minimizeFill.setFill(Color.LIMEGREEN);
				close.getChildren().add(closeFill);
				minimize.getChildren().add(minimizeFill);
				expand.getChildren().add(expandFill);

				final FillTransition ftclose = new FillTransition(Duration.seconds(animationDuration), closeFill),
						ftminimize = new FillTransition(Duration.seconds(animationDuration), minimizeFill),
						ftexpand = new FillTransition(Duration.seconds(animationDuration), expandFill);

				close.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					ftclose.stop();
					ftclose.setFromValue((Color) closeFill.getFill());
					ftclose.setToValue(Color.WHITE);
					ftclose.play();
				});

				close.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					ftclose.stop();
					ftclose.setFromValue((Color) closeFill.getFill());
					ftclose.setToValue(Color.CORAL);
					ftclose.play();
				});

				expand.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					ftexpand.stop();
					ftexpand.setFromValue((Color) expandFill.getFill());
					ftexpand.setToValue(Color.WHITE);
					ftexpand.play();
				});

				expand.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					ftexpand.stop();
					ftexpand.setFromValue((Color) expandFill.getFill());
					ftexpand.setToValue(WARNING_COLOR);
					ftexpand.play();
				});

				minimize.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					ftminimize.stop();
					ftminimize.setFromValue((Color) minimizeFill.getFill());
					ftminimize.setToValue(Color.WHITE);
					ftminimize.play();
				});

				minimize.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					ftminimize.stop();
					ftminimize.setFromValue((Color) minimizeFill.getFill());
					ftminimize.setToValue(Color.LIMEGREEN);
					ftminimize.play();
				});

			}

		}

		close.setOnMouseClicked(this::onClose);
		expand.setOnMouseClicked(this::onMaximize);
		minimize.setOnMouseClicked(this::onMinimize);

		// Menu bar

		new Object() {

			private double dx, dy;

			{
				menuBar.setOnMousePressed(event -> {
					if (isMaximized() || isFullScreen())
						return;
					dx = getX() - event.getScreenX();
					dy = getY() - event.getScreenY();
					event.consume();
				});

				menuBar.setOnMouseDragged(event -> {
					if (isMaximized() || isFullScreen())
						return;
					setX(event.getScreenX() + dx);
					setY(event.getScreenY() + dy);
					event.consume();
				});
			}
		};

		menuBar.setBorder(new Border(new BorderStroke(null, null, BACKGROUND_COLOR, null, null, null,
				BorderStrokeStyle.SOLID, null, null, new BorderWidths(2), null)));
		menuBar.setBackground(background);
		menuBar.setMaxHeight(30);
		menuBar.setPrefHeight(30);
		menuBar.setMinHeight(30);
		menuBar.setSpacing(2);
		if (OS.getOS() == OS.WINDOWS)
			menuBar.setAlignment(Pos.CENTER_RIGHT);
		else
			menuBar.setAlignment(Pos.CENTER_LEFT);

		final Border border = new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2, 2, 2, 2)));

		// Root
		root.setBorder(border);
		root.setTop(menuBar);
		new ResizeOperator(root, new Resizable() {

			@Override
			public void expandHor(final double amount) {
				if (getWidth() + amount < 600)
					setWidth(600);
				else
					setWidth(getWidth() + amount);
			}

			@Override
			public void expandVer(final double amount) {
				if (getHeight() + amount < 400)
					setHeight(400);
				else
					setHeight(getHeight() + amount);
			}

			@Override
			public double getX() {
				return ChatRoomWindow.this.getX();
			}

			@Override
			public double getY() {
				return ChatRoomWindow.this.getY();
			}

			@Override
			public void moveX(final double amount) {
				ChatRoomWindow.this.setX(ChatRoomWindow.this.getX() + amount);
			}

			@Override
			public void moveY(final double amount) {
				ChatRoomWindow.this.setY(ChatRoomWindow.this.getY() + amount);
			}
		}, 10) {

			public void addBar() {
				root.setBorder(new Border(
						new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2, 2, 8, 2))));
			}

			@Override
			public void handle(final MouseEvent event) {
				super.handle(event);
				if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED) && bottom(event))
					addBar();
				else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED))
					removeBar();
				else if (event.getEventType().equals(MouseEvent.MOUSE_MOVED))
					if (bottom(event))
						addBar();
					else
						removeBar();

			}

			public void removeBar() {
				root.setBorder(border);
			}
		};

		// Last bit of initialization:
		setWidth(800);
		setHeight(600);
	}

	protected void onClose(final MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY))
			close();
	}

	protected void onMaximize(final MouseEvent event) {
		if (event.getButton().equals(MouseButton.MIDDLE)) {
			setMaximized(false);
			setFullScreen(!isFullScreen());
		} else if (event.getButton().equals(MouseButton.PRIMARY))
			if (isFullScreen()) {
				setMaximized(false);
				setFullScreen(false);
			} else
				setMaximized(!isMaximized());

	}

	protected void onMinimize(final MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY))
			setIconified(true);
	}

	public void setMenubarType(final boolean full) {
		if (full)
			if (menuBar.getChildren().size() == 3)
				return;
			else
				menuBar.getChildren().addAll(minimize, expand);
		else if (menuBar.getChildren().size() == 1)
			return;
		else
			menuBar.getChildren().removeAll(minimize, expand);
	}

}
