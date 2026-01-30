/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.galaxy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ListShapeParam;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Spiral Arms
final class GalaxyRandomShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "RANDOMIZED";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	public	static final String REAL_SHAPES			= UI_KEY + SHORT_NAME + "_REAL_SHAPES";
	public	static final String IMAGINARY_SHAPES	= UI_KEY + SHORT_NAME + "_IMAGINARY_SHAPES";
	public	static final String COMPLEX_SHAPES		= UI_KEY + SHORT_NAME + "_COMPLEX_SHAPES";
	public	static final String EXTRA_SHAPES		= UI_KEY + SHORT_NAME + "_EXTRA_SHAPES";
	public	static final String ALL_SHAPES			= UI_KEY + SHORT_NAME + "_ALL_SHAPES";
	private	static final String DEFAULT_OPT_1		= COMPLEX_SHAPES;
	private	static final String KEEP_OPTIONS		= UI_KEY + SHORT_NAME + "_KEEP_OPTIONS";
	private	static final String RANDOM_OPTIONS_1	= UI_KEY + SHORT_NAME + "_RANDOM_OPTIONS_1";
	private	static final String RANDOM_OPTIONS_2	= UI_KEY + SHORT_NAME + "_RANDOM_OPTIONS_2";
	private	static final String RANDOM_OPTIONS		= UI_KEY + SHORT_NAME + "_RANDOM_OPTIONS";
	private	static final String DEFAULT_OPT_2		= RANDOM_OPTIONS;
	private static ShapeOptionList param1;
	private static ShapeOptionList param2;

	private static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				REAL_SHAPES,
				IMAGINARY_SHAPES,
				COMPLEX_SHAPES,
				ALL_SHAPES
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}
	private static ShapeOptionList param2()	{
		if (param2 == null) {
			param2 = new ShapeOptionList(
			BASE_NAME, 2,
			new ArrayList<String>(Arrays.asList(
				KEEP_OPTIONS,
				RANDOM_OPTIONS_1,
				RANDOM_OPTIONS_2,
				RANDOM_OPTIONS
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}
	private GalaxyShape finalShape;

	GalaxyRandomShape(IGameOptions options, boolean[] rndOpt)	{
		super(options, rndOpt);
		if (options == null)
			return; // BR: used to create empty shape to query shape info
		newRandomizedShape();
	}
	private void newRandomizedShape()	{
		finalShape = null;
		initFinalOption1();
		initFinalOption2();
		List<String> names	= AllShapes.getNamesForRandom(finalOption1);
		String shapeName	= randRnd.random(names);
		finalShape = AllShapes.getShape(shapeName, opts, randomizeShapeOptions);
	}

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public IShapeOption<?> paramOption2()	{ return param2(); }
	@Override public IShapeOption<?> paramOption3()	{ return null; }
	@Override public IShapeOption<?> paramOption4()	{ return null; }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()	{ return param1().getOptions(); }
	@Override public List<String> options2()	{ return param2().getOptions(); }
	@Override public String name()				{ return finalShape==null? NAME : finalShape.name(); }
	@Override public GalaxyShape get()			{ return this; }
	@Override public float maxScaleAdj()		{ return finalShape.maxScaleAdj(); }
	@Override public void init(int n)			{ finalShape.init(n); }
	@Override boolean fullGenerate()			{
		init0();
		newRandomizedShape();
		return finalShape.fullGenerate();
	}
	@Override public boolean quickGenerate()	{
		init0();
		newRandomizedShape();
		return finalShape.quickGenerate();
	}

	@Override public int width()				{ return finalShape.width(); }
	@Override public int height()				{ return finalShape.height(); }
	@Override protected boolean fullyInit()		{ return finalShape.fullyInit(); }
	@Override protected void initFinalOption1()	{
		if (finalShape==null) {
			finalOption1 = getOption1();
			option1 = max(0, options1().indexOf(finalOption1));
		}
		else
			finalShape.initFinalOption1();
	}
	@Override protected void initFinalOption2()	{
		if (finalShape==null) {
			finalOption2 = getOption2();
			option2 = max(0, options2().indexOf(finalOption2));
			switch (finalOption2) {
			case KEEP_OPTIONS:
				return;
			case RANDOM_OPTIONS_1:
				randomizeShapeOptions[0] = true;
				return;
			case RANDOM_OPTIONS_2:
				randomizeShapeOptions[1] = true;
				return;
			case RANDOM_OPTIONS:
				randomizeShapeOptions[0] = true;
				randomizeShapeOptions[1] = true;
				randomizeShapeOptions[2] = true;
				randomizeShapeOptions[3] = true;
				return;
			}
		}
		else
			finalShape.initFinalOption2();
	}
	@Override protected void initFinalOption3()	{
		if (finalShape==null)
			super.initFinalOption3();
		else
			finalShape.initFinalOption3();
	}
	@Override protected void initFinalOption4()	{
		if (finalShape==null)
			super.initFinalOption4();
		else
			finalShape.initFinalOption4();
	}
	@Override public ListShapeParam paramList()	{ return super.paramList(); }
	@Override public int numOptions1()			{ return super.numOptions1(); }
	@Override public int numOptions2()			{ return super.numOptions1(); }
	@Override public String getOption1()		{ return super.getOption1(); }
	@Override public String getOption2()		{ return super.getOption2(); }
	@Override public String getOption3()		{ return super.getOption3(); }
	@Override public String getOption4()		{ return super.getOption4(); }
	@Override public boolean valid(float x, float y)	{ return finalShape.valid(x, y); }
	@Override protected void clean()					{ finalShape.clean(); }
	@Override protected float settingsFactor(float shF)	{ return finalShape.settingsFactor(shF); }
	@Override protected float densitySizeFactor()		{ return finalShape.densitySizeFactor(); }
	@Override protected float minEmpireFactor()			{ return finalShape.minEmpireFactor(); }
	@Override protected boolean allowExtendedPreview()	{ return finalShape.allowExtendedPreview(); }
	@Override public boolean isSymmetric()				{ return finalShape.isSymmetric(); }
	@Override public boolean isCircularSymmetric()		{ return finalShape.isCircularSymmetric(); }
	@Override public boolean isRectangulatSymmetric()	{ return finalShape.isRectangulatSymmetric(); }
	@Override public CtrPoint getPlayerSymmetricHomeWorld()	{ return finalShape.getPlayerSymmetricHomeWorld(); }
	@Override public CtrPoint getValidRandomSymmetric()		{ return finalShape.getValidRandomSymmetric(); }
	@Override public int numCompanionWorld()			{ return finalShape.numCompanionWorld(); }
	@Override public int numberStarSystems()			{ return finalShape.numberStarSystems(); }
	@Override public int finalNumberStarSystems()		{ return finalShape.finalNumberStarSystems(); }
	@Override public int numEmpires()					{ return finalShape.numEmpires(); }
	@Override int totalStarSystems()					{ return finalShape.totalStarSystems(); }
	@Override public List<EmpireSystem> empireSystems()	{ return finalShape.empireSystems(); }
	@Override public Point.Float getCompanion(int eId, int cId)	{ return finalShape.getCompanion(eId, cId); }
	@Override public void coords(int n, Point.Float pt)	{ finalShape.coords(n, pt); }
	@Override public float empireBuffer()				{ return finalShape.empireBuffer(); }
	@Override public void createNebulas(List<Nebula> n)	{ finalShape.createNebulas(n); }
	@Override protected int galaxyWidthLY()				{ return finalShape.galaxyWidthLY(); }
	@Override protected int galaxyHeightLY()			{ return finalShape.galaxyHeightLY(); }
	@Override public void setRandom(Point.Float pt)		{ finalShape.setRandom(pt); }
	@Override protected void setSpecific(Point.Float p)	{ finalShape.setSpecific(p); }
	@Override protected float sizeFactor(String size)	{ return finalShape.sizeFactor(size); }
	@Override public boolean isValid()					{ return finalShape.isValid(); }
}
