package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.ChatRoom;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public abstract class InfoButton<T extends Node> extends StackPane {

	protected final Rectangle background = new Rectangle();
	private Duration transitionExitDuration;

	private T graphic;

	public void setGraphic(T graphic) {
		if (graphic != null)
			getChildren().remove(graphic);
		getChildren().add(graphic);
	}

	public T getGraphic() {
		return graphic;
	}

	{
		background.widthProperty().bind(widthProperty());
		background.heightProperty().bind(heightProperty());
		getChildren().addAll(background);
		setSize(16);

		background.setStroke(ChatRoom.DEFAULT_WINDOW_BORDER_COLOR);
		background.setStrokeWidth(2);
		background.setFill(Color.TRANSPARENT);
		background.setArcHeight(20);
		background.setArcWidth(20);

		Transition hoverTransition = new Transition() {

			{
				setCycleDuration(Duration.seconds(0.5));
			}

			@Override
			protected void interpolate(double frac) {
				background.setStroke(Color.BLACK.interpolate(Color.WHITE, frac));
				background.setArcHeight(getHeight() + 4 - (getHeight() + 4) * frac);
				background.setArcWidth(getWidth() + 4 - (getWidth() + 4) * frac);
			}
		};

		setOnMouseEntered(event -> {
			hoverTransition.pause();
			transitionExitDuration = hoverTransition.getCurrentTime();
			hoverTransition.stop();

			hoverTransition.setRate(1);
			hoverTransition.playFrom(transitionExitDuration);
		});

		setOnMouseExited(event -> {
			hoverTransition.pause();
			transitionExitDuration = hoverTransition.getCurrentTime();
			hoverTransition.stop();

			hoverTransition.setRate(-1);
			hoverTransition.playFrom(transitionExitDuration);
		});

		setOnMouseClicked(this::onClick);
	}

	protected abstract void onClick(MouseEvent event);

	public void setBackground(Paint paint) {
		background.setFill(paint);
	}

	public void setRounded(boolean rounded) {
		background.setArcHeight(rounded ? 4 : 0);
		background.setArcWidth(rounded ? 4 : 0);
	}

	public void setSize(double size) {
		setPrefSize(size, size);
		setMaxSize(size, size);
		setMinSize(size, size);
		background.setArcHeight(getPrefHeight() + 4);
		background.setArcWidth(getPrefWidth() + 4);
	}

}
