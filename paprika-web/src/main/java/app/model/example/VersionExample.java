package app.model.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.model.CodeSmells;
import app.model.Version;

/**
 * VersionExample is used when a new account have be created, for put a example.
 * 
 * @author guillaume
 *
 */
@SuppressWarnings("javadoc")
public class VersionExample extends Version {

	private boolean isFirst;

	/**
	 * @param name
	 *            name of the version
	 * @param id
	 *            id of the version
	 */
	public VersionExample(String name, long id, boolean first) {
		super(name, id);
		isFirst = first;
		this.analyzed=3;
	}

	@Override
	public long getNumberCodeSmells() {
		return isFirst ? 57 : 61;
	}

	@Override
	public int checkAnalyzed() {
		this.analyzed=3;
		return this.analyzed;
	}


	@Override
	public String getAnalyseInLoading() {
		return "100";
	}

	@Override
	public Iterator<CodeSmells> getAllCodeSmells() {
		List<CodeSmells> listNode = new ArrayList<>();
		CodeSmellsExample csexample;

		csexample = new CodeSmellsExample("NLMR", -1, 1);
		csexample.setLineList(
				"<tr><th>Class</th><th>Location</th><th>Modifier</th></tr><tr><td>AndroidLauncher</td><td>org.example.test.AndroidLauncher</td><td>\"public\"</td></tr>");
		listNode.add(csexample);
		csexample = new CodeSmellsExample("LIC", -1, (isFirst) ? 18 : 20);
		csexample.setLineList((isFirst)
				? "<tr><th>Class</th><th>Location</th><th>Modifier</th></tr><tr><td>PauseMenuStage$5</td><td>org.example.test.screens.PauseMenuStage$5</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$3</td><td>org.example.test.screens.MainMenuScreen$3</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$2</td><td>org.example.test.screens.CustomizeScreen$2</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$1</td><td>org.example.test.screens.PauseMenuStage$1</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$1</td><td>org.example.test.screens.MainMenuScreen$1</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$1</td><td>org.example.test.screens.CustomizeScreen$1</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$5</td><td>org.example.test.screens.CustomizeScreen$5</td><td>\"private\"</td></tr><tr><td>PieceHolder$DropResult</td><td>org.example.test.game.PieceHolder$DropResult</td><td>\"public\"</td></tr><tr><td>MainMenuScreen$2</td><td>org.example.test.screens.MainMenuScreen$2</td><td>\"private\"</td></tr><tr><td>MoneyBuyBand$2</td><td>org.example.test.actors.MoneyBuyBand$2</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$4</td><td>org.example.test.screens.MainMenuScreen$4</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$3</td><td>org.example.test.screens.PauseMenuStage$3</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$4</td><td>org.example.test.screens.PauseMenuStage$4</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$4</td><td>org.example.test.screens.CustomizeScreen$4</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$6</td><td>org.example.test.screens.CustomizeScreen$6</td><td>\"private\"</td></tr><tr><td>MoneyBuyBand$1</td><td>org.example.test.actors.MoneyBuyBand$1</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$3</td><td>org.example.test.screens.CustomizeScreen$3</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$2</td><td>org.example.test.screens.PauseMenuStage$2</td><td>\"private\"</td></tr>"
				: "<tr><th>Class</th><th>Location</th><th>Modifier</th></tr><tr><td>AndroidShareChallenge$1</td><td>org.example.test.AndroidShareChallenge$1</td><td>\"private\"</td></tr><tr><td>MoneyBuyBand$1</td><td>org.example.test.actors.MoneyBuyBand$1</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$4</td><td>org.example.test.screens.CustomizeScreen$4</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$1</td><td>org.example.test.screens.CustomizeScreen$1</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$5</td><td>org.example.test.screens.PauseMenuStage$5</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$2</td><td>org.example.test.screens.CustomizeScreen$2</td><td>\"private\"</td></tr><tr><td>MoneyBuyBand$2</td><td>org.example.test.actors.MoneyBuyBand$2</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$4</td><td>org.example.test.screens.MainMenuScreen$4</td><td>\"private\"</td></tr><tr><td>PieceHolder$DropResult</td><td>org.example.test.game.PieceHolder$DropResult</td><td>\"public\"</td></tr><tr><td>PauseMenuStage$4</td><td>org.example.test.screens.PauseMenuStage$4</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$6</td><td>org.example.test.screens.PauseMenuStage$6</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$3</td><td>org.example.test.screens.CustomizeScreen$3</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$5</td><td>org.example.test.screens.CustomizeScreen$5</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$1</td><td>org.example.test.screens.MainMenuScreen$1</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$1</td><td>org.example.test.screens.PauseMenuStage$1</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$3</td><td>org.example.test.screens.MainMenuScreen$3</td><td>\"private\"</td></tr><tr><td>CustomizeScreen$6</td><td>org.example.test.screens.CustomizeScreen$6</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$3</td><td>org.example.test.screens.PauseMenuStage$3</td><td>\"private\"</td></tr><tr><td>MainMenuScreen$2</td><td>org.example.test.screens.MainMenuScreen$2</td><td>\"private\"</td></tr><tr><td>PauseMenuStage$2</td><td>org.example.test.screens.PauseMenuStage$2</td><td>\"private\"</td></tr>");
		listNode.add(csexample);
		csexample = new CodeSmellsExample("IGS", -1, 2);
		csexample.setLineList(
				"<tr><th>Method</th><th>Location</th><th>Modifier</th><th>Type</th></tr><tr><td>addMoneyFromScore</td><td>org.example.test.Main</td><td>\"public\"</td><td>\"void\"</td></tr><tr><td>buyTheme</td><td>org.example.test.Main</td><td>\"public\"</td><td>\"boolean\"</td></tr>");
		listNode.add(csexample);
		csexample = new CodeSmellsExample("MIM", -1, 5);
		csexample.setLineList(
				"<tr><th>Method</th><th>Location</th><th>Modifier</th><th>Type</th><th>Number_of_direct_calls</th></tr><tr><td>pause</td><td>org.example.test.game.BaseScorer</td><td>\"public\"</td><td>\"void\"</td><td>0</td></tr><tr><td>dispose</td><td>org.example.test.Theme</td><td>\"private\"</td><td>\"void\"</td><td>0</td></tr><tr><td>scoreToNanos</td><td>org.example.test.game.TimeScorer</td><td>\"private\"</td><td>\"long\"</td><td>0</td></tr><tr><td>resume</td><td>org.example.test.game.BaseScorer</td><td>\"public\"</td><td>\"void\"</td><td>0</td></tr><tr><td>gameOverReason</td><td>org.example.test.game.BaseScorer</td><td>\"public\"</td><td>\"java.lang.String\"</td><td>0</td></tr>");
		listNode.add(csexample);
		csexample = new CodeSmellsExample("BLOB", -1, 2);
		csexample.setLineList(
				"<tr><th>Class</th><th>Location</th><th>Modifier</th><th>Lack_of_cohesion_in_methods</th><th>Number_of_attributes</th><th>Number_of_methods</th></tr><tr><td>Theme</td><td>org.example.test.Theme</td><td>\"public\"</td><td>98</td><td>14</td><td>16</td></tr><tr><td>GameScreen</td><td>org.example.test.screens.GameScreen</td><td>\"private\"</td><td>233</td><td>15</td><td>27</td></tr>");
		listNode.add(csexample);
		csexample = new CodeSmellsExample("LM", -1, (isFirst) ? 24 : 26);
		csexample.setLineList((isFirst)
				? "<tr><th>Method</th><th>Location</th><th>Modifier</th><th>Type</th><th>Number_of_lines</th></tr><tr><td>draw</td><td>org.example.test.actors.ThemeCard</td><td>\"public\"</td><td>\"void\"</td><td>27</td></tr><tr><td>deserialize</td><td>org.example.test.serializer.BinSerializer</td><td>\"public\"</td><td>\"void\"</td><td>23</td></tr><tr><td>clearComplete</td><td>org.example.test.game.Board</td><td>\"public\"</td><td>\"int\"</td><td>59</td></tr><tr><td>draw</td><td>org.example.test.game.BaseScorer</td><td>\"public\"</td><td>\"void\"</td><td>22</td></tr><tr><td>Board</td><td>org.example.test.game.Board</td><td>\"public\"</td><td>\"void\"</td><td>23</td></tr><tr><td>render</td><td>org.example.test.screens.GameScreen</td><td>\"public\"</td><td>\"void\"</td><td>19</td></tr><tr><td>loadSkin</td><td>org.example.test.SkinLoader</td><td>\"private\"</td><td>\"com.badlogic.gdx.scenes.scene2d.ui.Skin\"</td><td>65</td></tr><tr><td><clinit></td><td>org.example.test.SkinLoader</td><td>\"private\"</td><td>\"void\"</td><td>41</td></tr><tr><td>PauseMenuStage</td><td>org.example.test.screens.PauseMenuStage</td><td>\"private\"</td><td>\"void\"</td><td>22</td></tr><tr><td>ThemeCard</td><td>org.example.test.actors.ThemeCard</td><td>\"public\"</td><td>\"void\"</td><td>19</td></tr><tr><td>CustomizeScreen</td><td>org.example.test.screens.CustomizeScreen</td><td>\"private\"</td><td>\"void\"</td><td>49</td></tr><tr><td>MoneyBuyBand</td><td>org.example.test.actors.MoneyBuyBand</td><td>\"public\"</td><td>\"void\"</td><td>23</td></tr><tr><td>PieceHolder</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>\"void\"</td><td>24</td></tr><tr><td>updatePiecesStartLocation</td><td>org.example.test.game.PieceHolder</td><td>\"private\"</td><td>\"void\"</td><td>22</td></tr><tr><td>dropPiece</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>\"org.example.test.game.PieceHolder$DropResult\"</td><td>20</td></tr><tr><td>update</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>\"void\"</td><td>31</td></tr><tr><td>GameScreen</td><td>org.example.test.screens.GameScreen</td><td>\"private\"</td><td>\"void\"</td><td>27</td></tr><tr><td>render</td><td>org.example.test.screens.TransitionScreen</td><td>\"public\"</td><td>\"void\"</td><td>26</td></tr><tr><td>Piece</td><td>org.example.test.game.Piece</td><td>\"private\"</td><td>\"void\"</td><td>57</td></tr><tr><td>Piece</td><td>org.example.test.game.Piece</td><td>\"private\"</td><td>\"void\"</td><td>28</td></tr><tr><td>update</td><td>org.example.test.Theme</td><td>\"private\"</td><td>\"org.example.test.Theme\"</td><td>46</td></tr><tr><td>MainMenuScreen</td><td>org.example.test.screens.MainMenuScreen</td><td>\"public\"</td><td>\"void\"</td><td>24</td></tr><tr><td>interpolateText</td><td>org.example.test.actors.MoneyBuyBand</td><td>\"private\"</td><td>\"void\"</td><td>30</td></tr><tr><td>getThemes</td><td>org.example.test.Theme</td><td>\"public\"</td><td>\"com.badlogic.gdx.utils.Array\"</td><td>29</td></tr>"
				: "<tr><th>Method</th><th>Location</th><th>Modifier</th><th>Type</th><th>Number_of_lines</th></tr><tr><td>render</td><td>org.example.test.screens.GameScreen</td><td>\"public\"</td><td>\"void\"</td><td>19</td></tr><tr><td>GameScreen</td><td>org.example.test.screens.GameScreen</td><td>\"private\"</td><td>\"void\"</td><td>27</td></tr><tr><td>PieceHolder</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>\"void\"</td><td>24</td></tr><tr><td>dropPiece</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>\"org.example.test.game.PieceHolder$DropResult\"</td><td>20</td></tr><tr><td>update</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>\"void\"</td><td>31</td></tr><tr><td>CustomizeScreen</td><td>org.example.test.screens.CustomizeScreen</td><td>\"private\"</td><td>\"void\"</td><td>62</td></tr><tr><td>updatePiecesStartLocation</td><td>org.example.test.game.PieceHolder</td><td>\"private\"</td><td>\"void\"</td><td>22</td></tr><tr><td>draw</td><td>org.example.test.actors.Band</td><td>\"public\"</td><td>\"void\"</td><td>19</td></tr><tr><td>interpolateText</td><td>org.example.test.actors.MoneyBuyBand</td><td>\"private\"</td><td>\"void\"</td><td>30</td></tr><tr><td>draw</td><td>org.example.test.game.BaseScorer</td><td>\"public\"</td><td>\"void\"</td><td>22</td></tr><tr><td>MoneyBuyBand</td><td>org.example.test.actors.MoneyBuyBand</td><td>\"public\"</td><td>\"void\"</td><td>23</td></tr><tr><td>clearComplete</td><td>org.example.test.game.Board</td><td>\"public\"</td><td>\"int\"</td><td>59</td></tr><tr><td>getThemes</td><td>org.example.test.Theme</td><td>\"public\"</td><td>\"com.badlogic.gdx.utils.Array\"</td><td>29</td></tr><tr><td>update</td><td>org.example.test.Theme</td><td>\"private\"</td><td>\"org.example.test.Theme\"</td><td>46</td></tr><tr><td>PauseMenuStage</td><td>org.example.test.screens.PauseMenuStage</td><td>\"private\"</td><td>\"void\"</td><td>24</td></tr><tr><td>Piece</td><td>org.example.test.game.Piece</td><td>\"private\"</td><td>\"void\"</td><td>57</td></tr><tr><td>saveChallengeImage</td><td>org.example.test.ShareChallenge</td><td>\"public\"</td><td>\"boolean\"</td><td>47</td></tr><tr><td><clinit></td><td>org.example.test.SkinLoader</td><td>\"private\"</td><td>\"void\"</td><td>41</td></tr><tr><td>MainMenuScreen</td><td>org.example.test.screens.MainMenuScreen</td><td>\"public\"</td><td>\"void\"</td><td>24</td></tr><tr><td>loadSkin</td><td>org.example.test.SkinLoader</td><td>\"private\"</td><td>\"com.badlogic.gdx.scenes.scene2d.ui.Skin\"</td><td>65</td></tr><tr><td>Board</td><td>org.example.test.game.Board</td><td>\"public\"</td><td>\"void\"</td><td>23</td></tr><tr><td>render</td><td>org.example.test.screens.TransitionScreen</td><td>\"public\"</td><td>\"void\"</td><td>26</td></tr><tr><td>deserialize</td><td>org.example.test.serializer.BinSerializer</td><td>\"public\"</td><td>\"void\"</td><td>23</td></tr><tr><td>draw</td><td>org.example.test.actors.ThemeCard</td><td>\"public\"</td><td>\"void\"</td><td>20</td></tr><tr><td>Piece</td><td>org.example.test.game.Piece</td><td>\"private\"</td><td>\"void\"</td><td>28</td></tr><tr><td>ThemeCard</td><td>org.example.test.actors.ThemeCard</td><td>\"public\"</td><td>\"void\"</td><td>19</td></tr>");
		listNode.add(csexample);
		csexample = new CodeSmellsExample("CC", -1, 5);
		csexample.setLineList(
				"<tr><th>Class</th><th>Location</th><th>Modifier</th><th>Class_complexity</th><th>Npath_complexity</th></tr><tr><td>Board</td><td>org.example.test.game.Board</td><td>\"public\"</td><td>56</td><td>7.2057594037927936E16</td></tr><tr><td>GameScreen</td><td>org.example.test.screens.GameScreen</td><td>\"private\"</td><td>53</td><td>9.007199254740992E15</td></tr><tr><td>PieceHolder</td><td>org.example.test.game.PieceHolder</td><td>\"public\"</td><td>46</td><td>7.0368744177664E13</td></tr><tr><td>Piece</td><td>org.example.test.game.Piece</td><td>\"public\"</td><td>45</td><td>3.5184372088832E13</td></tr><tr><td>Main</td><td>org.example.test.Main</td><td>\"public\"</td><td>30</td><td>1.073741824E9</td></tr>");
		listNode.add(csexample);

		return listNode.iterator();
	}

}
