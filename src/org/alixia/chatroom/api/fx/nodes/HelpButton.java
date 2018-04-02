package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.QuickList;

import javafx.animation.Transition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class HelpButton extends StackPane {

	public static final Paint SIMPLE_BLUE = new RadialGradient(0, 0, 16, 0, 8, false, CycleMethod.NO_CYCLE,
			new QuickList<>(new Stop(0, Color.WHITE), new Stop(0.2, Color.SLATEBLUE)));

	private final Rectangle background = new Rectangle();
	private final Text questionMark = new Text("?");

	private Duration transitionExitDuration;

	{
		background.widthProperty().bind(widthProperty());
		background.heightProperty().bind(heightProperty());
		getChildren().addAll(background, questionMark);
		setPrefSize(16, 16);

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

	}

	public void setBackground(Paint paint) {
		background.setFill(paint);
	}

	public void setRounded(boolean rounded) {
		background.setArcHeight(rounded ? 4 : 0);
		background.setArcWidth(rounded ? 4 : 0);
	}

	public void setSize(double size) {
		setWidth(size);
		setHeight(size);
		background.setArcHeight(getHeight() + 4);
		background.setArcWidth(getWidth() + 4);
	}

}
