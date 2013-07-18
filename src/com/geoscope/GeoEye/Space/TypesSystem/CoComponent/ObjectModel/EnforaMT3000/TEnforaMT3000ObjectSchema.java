package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TComponentSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt32Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentObjectDescriptorValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentXYCrdValue;

public class TEnforaMT3000ObjectSchema extends TComponentSchema {

	public class TEnforaMT3000ObjectComponent extends TComponent
	{
		public class TVisualizationComponent extends TComponent
		{
			public TComponentObjectDescriptorValue 	Descriptor;
			public TComponentXYCrdValue 			LastXYPosition;
			
			public TVisualizationComponent(TComponent pOwner, int pID) 
			{
				super(pOwner,pID,"Visualization");
				//.
				Descriptor		= new TComponentObjectDescriptorValue(this,1,"Descriptor");
				LastXYPosition	= new TComponentXYCrdValue(this,2,"LastXYPosition");
			}
		}
		
		public TComponentInt32Value 			GeoSpaceID;
		public TVisualizationComponent		 	Visualization;
		public TComponentInt32Value 			Hint;
		public TComponentInt32Value 			UserAlert;
		public TComponentInt32Value 			OnlineFlag;
		public TComponentInt32Value 			LocationIsAvailableFlag;
		
		public TEnforaMT3000ObjectComponent()
		{
			super(TEnforaMT3000ObjectSchema.this,1,"EnforaMT3000ObjectComponent");
			//. items
			GeoSpaceID				= new TComponentInt32Value		(this,1,"GeoSpaceID");
			Visualization			= new TVisualizationComponent	(this,2);
			Hint 					= new TComponentInt32Value		(this,3,"Hint");
			UserAlert 				= new TComponentInt32Value		(this,4,"UserAlert");
			OnlineFlag 				= new TComponentInt32Value		(this,5,"OnlineFlag");
			LocationIsAvailableFlag = new TComponentInt32Value		(this,6,"LocationIsAvailableFlag");
		}
	}
	
	public TEnforaMT3000ObjectSchema(TObjectModel pObjectModel) {
		super(pObjectModel);
		RootComponent = new TEnforaMT3000ObjectComponent();
	}
}
